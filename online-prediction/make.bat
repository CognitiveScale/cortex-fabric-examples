@echo off

for /f %%q in ("%~dp0.") do SET SKILL_NAME=%%~nxq

SET IMAGE_TAG=latest
SET DOCKER_IMAGE_URL=%DOCKER_PREGISTRY_URL%/%SKILL_NAME%:%IMAGE_TAG%

GOTO :checkenv %*

:init
    cmd /c cortex models save model/model.json --project %PROJECT_NAME%
    cmd /c cortex experiments save model/experiment.json --project %PROJECT_NAME%
    cmd /c cortex experiments create-run model/run.json --project %PROJECT_NAME%
    GOTO :EOF

:build
	echo Building %SKILL_NAME% action image...
    cmd /c docker build -t %DOCKER_IMAGE_URL% .
	echo done building %SKILL_NAME% action image
	GOTO :EOF

:push
	echo Pushing %SKILL_NAME% action image ...
    :: cmd /c cortex docker login
    cmd /c docker push %DOCKER_IMAGE_URL%
	echo done pushing %SKILL_NAME% action image
	GOTO :EOF

:deploy
	echo Deploying %SKILL_NAME% ...
    cmd /c cortex actions deploy --actionName %SKILL_NAME% --actionType daemon --project %PROJECT_NAME% --docker %DOCKER_IMAGE_URL%
    cmd /c cortex types save -y types/types.yaml --project %PROJECT_NAME%
    cmd /c cortex skills save -y skill.yaml --project %PROJECT_NAME%
	echo Waiting for Skill %SKILL_NAME% to deploy
	TIMEOUT 10
	echo Deployed %SKILL_NAME%. Check "actionStatuses" for deployment status
	GOTO :get

:tests
	echo Testing %SKILL_NAME%
    cmd /c cortex skills invoke --params-file ./test/payload.json %SKILL_NAME% request --project %PROJECT_NAME%
	GOTO :EOF

:get
    cmd /c cortex skills describe %SKILL_NAME%  --verbose --project %PROJECT_NAME%
	GOTO :EOF

:checkenv
echo Validating environment variables
IF not defined DOCKER_PREGISTRY_URL (
	echo "Environment variable DOCKER_PREGISTRY_URL is not set. Set it like <docker-registry-url>/<namespace-org>"
	GOTO :EOF
)
IF not defined PROJECT_NAME (
	echo "Environment variable PROJECT_NAME is not set. Set this to Cortex project name."
	GOTO :EOF
)
GOTO :%1
