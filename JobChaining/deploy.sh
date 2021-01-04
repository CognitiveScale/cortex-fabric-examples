#!/bin/bash -eu
echo "## Building docker images, then deploying actions, skills, and agent to cortex"
DOCKER_PUSH=${DOCKER_PUSH:-"true"}
# we'll assume each skill folder looks like:
# skills/
#    skillname/
#       skill.yaml
#       Dockerfile

# Check for cortex cli, docker, and cortex login
which cortex > /dev/null || ( echo "Cortex command line is required 'npm install -g cortex-cli'" && exit 1 )
which docker > /dev/null || ( echo "Docker command line is required https://docs.docker.com/get-docker/" && exit 1 )
cortex agents list > /dev/null || ( echo "Unable to connect to cortex, perhaps re-run 'cortex configure'" && exit 1 )
if [ "${DOCKER_PUSH}" != "true" ]; then
  echo "!!! Docker images must be manually pushed to a registry !!!"
fi
# Build docker image(s) then deploy skills, and actions to cortex instance.
function actionSkillInstall() {
  echo "## Building skills in skills/"
  for SKILLYAML in skills/**/skill.yaml; do
    SKILLDIR=$(dirname $SKILLYAML)
    SKILLNAME=$(basename $SKILLDIR)
    echo "## Building ${SKILLDIR}"
    # build the docker image, cortex's cli will retag/push to the server.
    docker build -t ${SKILLNAME} -f "${SKILLDIR}/Dockerfile" "${SKILLDIR}"
    # create action and push image to cortex server
    if [ "${DOCKER_PUSH}" == "true" ]; then
      cortex actions deploy --actionName cortex/${SKILLNAME} --actionType job --push-docker --docker ${SKILLNAME}
    else
      cortex actions deploy --actionName cortex/${SKILLNAME} --actionType daemon --port 5000  --docker ${SKILLNAME}
    fi
    # save skill definition
    cortex skills save ${SKILLDIR}/skill.yaml --yaml
  done

}

# Deploy skills/actions
actionSkillInstall

# Deploy agent
cortex agents save agent.yaml --yaml

echo "## Agent, skill(s), action(s) deployed"
