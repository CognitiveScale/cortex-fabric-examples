#!/bin/bash -eu
SCRIPT_PATH="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
VERSION=$(awk '/version/{gsub(/("|",)/,"",$2);print $2};' ${SCRIPT_PATH}/../package.json)

IFS='-' read -r TAG REMAINDER <<< "${VERSION}"
NUMBER_REGEX="^[0-9]+$"
if [[ ${REMAINDER} =~ ${NUMBER_REGEX} ]]; then
    PRERELEASE=".post${REMAINDER}"
else
    IFS='.' read -r PREID REMAINDER <<< "${REMAINDER}"
    if [[ ${PREID} ]]; then
        case ${PREID} in
        "alpha")
            PRERELEASE="a${REMAINDER}"
            ;;
        "beta")
            PRERELEASE="b${REMAINDER}"
            ;;
        "rc")
            PRERELEASE="rc${REMAINDER}"
            ;;
        *)
            PRERELEASE=".dev${REMAINDER}"
            ;;
        esac
    else
        PRERELEASE=""
    fi
fi

PACKAGE="${TAG}${PRERELEASE}"
echo "Setting Python package version to: ${PACKAGE}"
VERSION_PATH="${SCRIPT_PATH}/../jupyterlab_cortex/_version.py"
echo "__version__ = '${PACKAGE}'" > ${VERSION_PATH}
