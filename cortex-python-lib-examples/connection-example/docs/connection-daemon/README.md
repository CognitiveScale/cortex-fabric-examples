### Cortex-Python Library Example: Connection Example

Cortex Skill (daemon) example for Cortex Connections. This demonstrates:
* Create Cortex Connections with Secrets
    * In the Cortex project, create Secrets `test-db-uri`, `test-db-name` and `test-db-collection` as per MongoDB server details
    * Create Cortex Connection (of type MongoDB) from Connection definition (test-connection.json), using `cortex connections save test-connection.json` command
* Use Connections in Skill
    * See `main.py` for how to use Cortex Connections in a Skill. This will create a connection and pool it for serving subsequent queries.  
    * Create Secrets and run `make all` to save connection, build & deploy Action/Skill, and test the deployed Skill 

#### Files to review
- `docs/` - The directory that houses the Skills' READMEs
  - `connection-daemon/`: The directory that houses the connection-daemon Skill's README
    - `README.md`: Provides the objectives, requirements, and instructions for generating and deploying the Skill.
- `connections/test-connection.json`: Connection definition
- `skills/`: The directory that houses the Skills
  - `connection-daemon/`: The directory that houses the connection-daemon Skill's assets
    - `actions/`: The directory that houses the connection-daemon Skill's Actions
      - `connection-daemon/`: The contents of the connection-daemon action
        - `Dockerfile`: Builds the Docker image the action
        - `main.py`: Code for Cortex job
        - `requirements.txt`: Dependencies and libraries
    - `invoke/`: Contains the payloads, organized by Skill input name, used to invoke the connection-daemon Skill
      - `request/`: Contains payload files used to invoke the Skill
        - `message.json`: JSON payload used to invoke the Skill
      - `skill.yaml`: connection-daemon Skill definition and Action mapping

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

For more details about how to build skills go to [Cortex Fabric Documentation - Development - Develop Skills](https://cognitivescale.github.io/cortex-fabric/docs/build-skills/define-skills).
