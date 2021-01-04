#!/bin/bash -eu
echo "## Deploy skill(s) and agent to cortex"
# we'll assume each skill folder looks like:
# skills/
#    skillname/
#       skill.yaml
#       Dockerfile

# Check for cortex cli, docker, and cortex login
which cortex > /dev/null || ( echo "Cortex command line is required 'npm install -g cortex-cli'" && exit 1 )
cortex agents list > /dev/null || ( echo "Unable to connect to cortex, perhaps re-run 'cortex configure'" && exit 1 )
# Build docker image(s) then deploy skills, and actions to cortex instance.
function actionSkillInstall() {
  echo "## Building skills in skills/"
  for SKILLYAML in skills/**/skill.yaml; do
    SKILLDIR=$(dirname $SKILLYAML)
    SKILLNAME=$(basename $SKILLDIR)
    echo "## Building ${SKILLDIR}"
    # save skill definition
    cortex skills save ${SKILLDIR}/skill.yaml --yaml
  done

}

# Deploy skills/actions
actionSkillInstall

# Deploy agent
cortex agents save agent.yaml --yaml

echo "## Agent, skill(s) deployed"
