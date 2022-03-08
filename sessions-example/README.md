### Sessions Skill Demo

Cortex skill demonstrating Sessions API

| Endpoint |                         Input                         |                Output                |              Description               |
|:--------:|:-----------------------------------------------------:|:------------------------------------:|:--------------------------------------:|
|  /start  | {"ttl": 10,<br/> "description":"session description"} | {"session_id": "created session id"} |    Start the session for given ttl     |
|   /get   |  {"session_id": "session id",<br/>"key": "data_key"}  |         session data by key          | get session data by session_id and key |
|   /put   |     {"session_id": "session id",<br/>"data": {}}      |           status response            |            put session data            |
| /delete  |      {"session_id": "session id to be deleted"}       |           status response            |    Delete the session by session_id    |


#### Files generated
* `skill.yaml` Skill definition
* `main.py` Python3 code serving the daemon API
* `requirements.txt` Python3 libraries dependencies
* `Dockerfile` to build Docker image for this skill

#### Steps

A Makefile is provided to do these steps. Set environment variables `DOCKER_PREGISTRY_URL` (like <docker-registry-url>/<namespace-org>) and `PROJECT_NAME` (Cortex Project Name) and use Makefile to deploy Skill.
`make all` will build and push Docker image, deploy Cortex Action and Skill, and then invoke Skill to test.

1. Modify the main executable (`main.py` by default) run by the action image's entrypoint/command to handle the action's custom logic.
2. Modify the `requirements.txt` file to provide packages or libraries that the action requires.
3. Build the docker image (uses the `main.py` file)
  ```
  docker build -t <image-name>:<version> .
  ```
4. Push the docker image to a registry that is connected to your Kubernetes cluster.
  ```
  docker push <image-name>:<version>
  ```

  **(Optionally) Retag an image**
  ```
  docker tag <existing-image-name>:<existing-version> <new-image-name>:<new-version>
  ```
5. Deploy the action.
  ```
  cortex actions deploy --actionName <SKILL_NAME> \
  --actionType daemon \
  --docker <DOCKER_IMAGE> \
  --port '5000'  \
  --project <Project Name>
  ```
6. Modify the `skill.yaml` file.
7. Save/deploy the Skill.
  ```
  cortex skills save -y skill.yaml --project <Project Name>
  ```

   The Skill is added to the Cortex Fabric catalog and is available for selection when building interventions or Agents.

   Skills that are deployed may be invoked (run) either independently or within an agent.

For more details about how to build skills go to [Cortex Fabric Documentation - Development - Develop Skills](https://cognitivescale.github.io/cortex-fabric/docs/build-skills/define-skills)
