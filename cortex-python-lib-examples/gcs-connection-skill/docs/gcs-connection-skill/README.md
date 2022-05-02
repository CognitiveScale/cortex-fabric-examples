TODO's:
- match file structure of existing examples
- add README & UPDATE SECRET NAMES & VALUES
- Replace test module with main script that runs everything with the Python Client/CLI
- Add description of example to top level README

### Cortex-Python Library Example: GCS Connection Example

Cortex Skill (job) example for working with a GCS Cortex Connection. This demonstrates:
* Creating a GCS Cortex connection with Secrets
	- In the Cortex project, create the secret `gcs-service-key`, by ...
	- Create the Cortex connection (of type GCS) from the connection definition (`connections/connection.json`), using
	`cortex connections save connections/connection.json`
* Use the Connection in a Skill
	- See `gcs-connection-skill/main.py` for how to use Cortex Connections in a Skill. This skill gets the connection from Cortex then uses the `google-cloud-storage` package (https://pypi.org/project/google-cloud-storage/) to download the contents of the connection
	- The skill is only accessing the data, but could be extended to perform additional operations on the data, such as training a model or transforming the data
	- Run `make all` to save the connection, build and deploy the action/skill, and test the deployed skill

#### Files to review
- `docs/` - The directory that houses the Skills' READMEs
  - `gcs-connection-skill/`: The directory that houses the Skill's README
    - `README.md`: Provides the objectives, requirements, and instructions for generating and deploying the Skill.
- `connections/connection.json`: GCS Connection definition
- `skills/`: The directory that houses the Skills
  - `gcs-connection-skill/`: The directory that houses the Skill's assets
	- `Dockerfile`: Builds the Docker image the action
	- `main.py`: Code for Cortex job
	- `requirements.txt`: Dependencies and libraries
- `skill.yaml`: connection-daemon Skill definition and Action mapping
- `Makefile`: connection-daemon Skill definition and Action mapping

#### Steps

Prerequisites:
* You have uploaded the German Credit Dataset (CSV) file, available in the [EndToEndExample/data folder](../EndToEndExample/data/german_credit_eval.csv) to GCS storage (see: https://cloud.google.com/storage/docs/uploading-objects?hl=en)
* You have created a GCS Service Account and downloaded a JSON key file that can access the above. You may have to grant certain roles to the service account, for reference see:
	- https://cloud.google.com/iam/docs/creating-managing-service-accounts#console
	- https://cloud.google.com/iam/docs/creating-managing-service-account-keys#console
	- https://cloud.google.com/iam/docs/manage-access-service-accounts
* The Cortex CLI is installed and configured

0. Create the Secret in the Cortex project. You can do either in the Cortex console or via the Cortex CLI by running: `cortex secrets save gcs_service_key --data-file <path to service_account.json>`
1. Create the Cortex Connection by running: `cortex connections save connections/connection.json`
2. Modify the main executable (`gcs-connection-skill/main.py`) run by the action image's entrypoint/command
3. Build the docker image for the action, by running `make build`
4. Push the docker image, by running `make push`
5. Save the action/skill, by running `make save-skill`
6. Invoke the skill

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
