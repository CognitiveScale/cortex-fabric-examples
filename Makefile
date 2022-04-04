SHELL := /bin/bash

.PHONY:build push deploy tests get all
EXAMPLES := ai-missions/high-risk-flu-shot-mission/skills batch-prediction online-prediction jdbc-example ExperimentsExample InterventionSkillExample
export PROJECT_NAME = test
export DOCKER_PREGISTRY_URL = test

build:
	@for exp in $(EXAMPLES); do (cd $$exp && $(MAKE) build) || exit 1; done