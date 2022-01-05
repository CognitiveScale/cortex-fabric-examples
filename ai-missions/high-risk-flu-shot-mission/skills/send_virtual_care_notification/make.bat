@echo off

SET IMAGE_TAG=latest
SET DOCKER_IMAGE=virtual-care
SET DOCKER_IMAGE_URL=%DOCKER_PREGISTRY_URL%/%DOCKER_IMAGE%:%IMAGE_TAG%

GOTO :checkenv %*


:build
	echo Building %DOCKER_IMAGE_URL% action image...
    xcopy /s ..\..\common .\src
    cmd /c docker build -t %DOCKER_IMAGE_URL% .
	echo done building %DOCKER_IMAGE% action image
	GOTO :EOF

:push
	echo Pushing %DOCKER_IMAGE% action image ...
    :: cmd /c cortex docker login
    cmd /c docker push %DOCKER_IMAGE_URL%
	echo done pushing %DOCKER_IMAGE% action image
	GOTO :EOF

:deploy
	echo Deploying %DOCKER_IMAGE% ...
    cmd /c cortex actions deploy --actionName hw/%DOCKER_IMAGE% --actionType daemon --port 6000 --cmd '[]' --podspec podspec.json --docker %DOCKER_IMAGE_URL% --project %PROJECT_NAME%
    cmd /c cortex types save -y types.yaml --project %PROJECT_NAME%
    cmd /c cortex skills save -y skill.yaml --project %PROJECT_NAME%
	echo Waiting for Skill %DOCKER_IMAGE% to deploy
	TIMEOUT 10
	echo Deployed %DOCKER_IMAGE%. Check "actionStatuses" for deployment status
	GOTO :get

:tests
	echo Testing %DOCKER_IMAGE%
    cmd /c cortex skills invoke --params-file ./tests/test_req.json hw/%DOCKER_IMAGE% request --project %PROJECT_NAME%
	GOTO :EOF

:get
    cmd /c cortex skills describe hw/%DOCKER_IMAGE%  --verbose --project %PROJECT_NAME%
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
