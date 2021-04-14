### Daemon Skill type

Cortex Skill (daemon) example for Cortex Connections. This demonstrates:
* Create Cortex Connections with Secrets
    * In the Cortex project, create Secrets `test-db-uri`, `test-db-name` and `test-db-collection` as per MongoDB server details
    * Create Cortex Connection (of type MongoDB) from Connection definition (test-connection.json), using `cortex connections save test-connection.json` command
* Use Connections in Skill
    * See `main.py` for how to use Cortex Connections in a Skill. 
    * Create Secrets and run `make all` to save connection, build & deploy Action/Skill, and test the deployed Skill 

#### Files to review
* `skill.yaml` Skill definition
* `main.py` Python3 code serving the daemon API
* `requirements.txt` Python3 libraries dependencies
* `Dockerfile` to build Docker image for this skill
* `Makefile` to setup and deploy Skill
* `test-connection.json` Connection definition

#### Steps

A Makefile is provided to do these steps. Set environment variables `DOCKER_PREGISTRY_URL` (like <docker-registry-url>/<namespace-org>) & `PROJECT_NAME` (Cortex Project Name) and use Makefile to deploy skill.
`make all` will build & push Docker image, deploy Cortex Action and Skill, and then invoke skill to test.

0. Create Secrets in the Cortex project and save connection `cortex connections save test-connection.json`
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

For more details about how to build skills go to [Cortex Fabric Documentation - Development - Develop Skills](https://cognitivescale.github.io/cortex-fabric/docs/development/define-skills)
