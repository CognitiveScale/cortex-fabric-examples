#!/bin/bash -eu
IMAGE_NAME=$(git remote -v | grep "(fetch)" | sed -E "s/.*git@.*:.*\/(.*)\.git.*/\1/")
BRANCH=$(git symbolic-ref --short -q HEAD)
VERSION=$(git describe --long --always --dirty --match='v*.*' | sed 's/v//')

function error_exit {
    echo "$1" >&2   ## Send message to stderr. Exclude >&2 if you don't want it that way.
    exit "${2:-1}"  ## Return a code specified by $2 or 1 by default.
}

function process_notebooks(){
    pip install -r requirements-dev.txt
    jupyter nbconvert --to markdown 'jupyterlab_cortex/notebooks/python3/**/**/*.ipynb'
    # test the notebooks in parallel, using xargs -P and excluding deployment notebooks
    find ./jupyterlab_cortex/notebooks -type f -name "*.ipynb" | grep -v deployment | xargs -P 4 -I var pytest --nbval-lax var
}

function local_build(){
    process_notebooks
    npm install || error_exit "Failed to run npm install"
    pip install twine
}

# This runs inside a linux docker container
function ci_build(){
    process_notebooks

    # NodeJS client extension build prereqs
    npm i -g npm@6.4.1
    npm -v
    npm config set loglevel warn
    npm cache clear --force
    npm install -dd --verbose

    # Python server extension build prereqs
    pip install twine

    if [[ ${BRANCH} = "master" ]]; then
        # NodeJS client extension build and publish
        npm publish --registry=https://registry.npmjs.com/
        # Python server extension build and publish
        python setup.py sdist --format=zip
        echo "Pushing to PyPI"
        twine upload --repository-url "${PYPI_UPLOAD}" --username "${REPO_USER}" --password "${REPO_PASSWORD}" dist/*.zip
    else
        # NodeJS client extension build and publish
        npm config set always-auth true
        npm publish --tag "${BRANCH}" --registry=https://cognitivescale.jfrog.io/cognitivescale/api/npm/npm-local/
        # Python server extension build and publish
        python setup.py sdist --format=zip
        echo "Pushing to local"
        twine upload -r "${REPO_NAME}" dist/*.zip
    fi
}

## MAIN
cd "$(dirname "$0")"
echo "##### BUILDING BRANCH[${BRANCH}],VERSION[${VERSION}] of IMAGE[${IMAGE_NAME}] ######"
case ${1-local} in
    CI)
        ci_build
        ;;
    *)
        local_build
        ;;
esac
