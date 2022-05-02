### Cortex-Python Library Example: GCS Connection Example

Cortex Skill (job) example for working with a GCS Cortex Connection. This demonstrates:
* Creating a Cortex Secret named `gcs-service-key`
* Creating a Cortex Connection to Google Cloud Storage (GCS)
* Using the GCS Connection in a Skill
	- See `gcs-connection-skill/main.py` for how to use a Cortex Connections in a Skill. The skill gets the connection from Cortex and uses the `google-cloud-storage` package (https://pypi.org/project/google-cloud-storage/) to read the contents of the connection.
  - The skill only access the data, but could be extended to perform data transformations or train a model, etc.

#### Files to review
- `docs/` - The directory that houses the Skills' READMEs
  - `gcs-connection-skill/`: The directory that houses the Skill's README
    - `README.md`: Provides the objectives, requirements, and instructions for generating and deploying the Skill.
- `connections/connection.json`: GCS Connection definition
- `skills/`: The directory that houses the Skills
  - `gcs-connection-skill/`: The directory that houses the Skill's assets
	- `Dockerfile`: Builds the Docker image for the action
	- `main.py`: Code for Cortex job
	- `requirements.txt`: Dependencies and libraries
- `payload.json`: File with JSON payload for invoking the skill
- `skill.yaml`: connection-daemon Skill definition and Action mapping
- `Makefile`: connection-daemon Skill definition and Action mapping

#### Steps

Prerequisites:
* You have uploaded the German Credit Dataset (CSV) file, available in the [EndToEndExample/data folder](../EndToEndExample/data/german_credit_eval.csv) to GCS storage (see: https://cloud.google.com/storage/docs/uploading-objects?hl=en).
* You have created a GCS Service Account and downloaded a JSON key file that can access the above. You may have to grant certain roles to the service account, for reference see:
	- https://cloud.google.com/iam/docs/creating-managing-service-accounts#console
	- https://cloud.google.com/iam/docs/creating-managing-service-account-keys#console
	- https://cloud.google.com/iam/docs/manage-access-service-accounts
  Save the JSON service account key to a file for later use, e.g. `service_account.json`.
* The Cortex CLI is installed and configured in your local environment.

A Makefile is provided to do help with the following steps. Before beginning, update the `DOCKER_PREGISTRY_URL` (e.g. <docker-registry-url>/<namespace-org>) and `PROJECT_NAME` (Cortex Project Name) environment variables within the Makefile. **If you update the `DOCKER_PREGISTRY_URL` in the Makefile, then make an equivalent update for the Action's `image` in the `skill.json` file.**

0. Create a secret named `gcs-service-key` in the Cortex project, with the service account key JSON file. You can do either in the Cortex console or via the Cortex CLI by running: `cortex secrets save gcs_service_key --data-file <path to service_account.json>`
1. Create the Cortex Connection by running: `make save-connection`
2. Build the docker image for the action, by running `make build`
3. Push the docker image, by running `make push`
4. Save the action/skill, by running `make deploy`
5. Invoke the skill by running `make tests`, e.g.
```
$ make tests
cortex skills invoke --params-file ./payload.json gcs-connection-skill params
{
  "success": true,
  "activationId": "4b6ce400-5c34-4cb4-8cfd-2ada7be09c93"
}
```

You alternatively run `make all` to run for steps 1 - 5. With the `activationId` above you can check for the status of the job by running `cortex agents get-activation <activationId>`. The skill invocation should result in a status of `COMPLETE` with the message `"Connection: gcs-german-credit read successfully"` being the Job response (logs).
```
$ cortex agents get-activation 4b6ce400-5c34-4cb4-8cfd-2fab3be09c93
{
  "success": true,
  "requestId": "4b6ce400-5c34-4cb4-8cfd-2fab3be09c93",
  "skillName": "gcs-connection-skill",
  "inputName": "params",
  "projectId": "test-project",
  "username": "user@example.com",
  "payload": {
    "connection_name": "gcs-german-credit"
  },
  "sessionId": "4b6ce400-5c34-4cb4-8cfd-2ada7be09c93",
  "start": 1651517957034,
  "status": "COMPLETE",
  "end": 1651517966356,
  "response": "2022-05-02T18:59:19Z scuttle: Scuttle 1.3.6 starting up, pid 1\n2022-05-02T18:59:19Z scuttle: Logging is now enabled\n2022-05-02T18:59:19Z scuttle: Blocking until Envoy starts\n2022-05-02T18:59:19Z scuttle: Polling Envoy (1), error: internal_service: dial tcp 127.0.0.1:15000: connect: connection refused\n2022-05-02T18:59:19Z scuttle: Blocking finished, Envoy has started\nConnection: gcs-german-credit read successfully\n2022-05-02T18:59:20Z scuttle: Kill received: (Action: Stopping Istio with API, Reason: ISTIO_QUIT_API is set, Exit Code: 0)\n2022-05-02T18:59:20Z scuttle: Stopping Istio using Istio API 'http://localhost:15020' (intended for Istio >v1.2)\n2022-05-02T18:59:20Z scuttle: Received signal 'child exited', passing to child\n2022-05-02T18:59:20Z scuttle: Sent quitquitquit to Istio, status code: 200\n"
}
```

Or by finding logs from the corresponding Task (check `cortex tasks list` for the `activationId`), e.g.
```
$ cortex tasks logs test-project-gcs-connection-skill-gcs-connection-rs2g2p
2022-05-02T18:59:19Z scuttle: Scuttle 1.3.6 starting up, pid 1
2022-05-02T18:59:19Z scuttle: Logging is now enabled
2022-05-02T18:59:19Z scuttle: Blocking until Envoy starts
2022-05-02T18:59:19Z scuttle: Polling Envoy (1), error: internal_service: dial tcp 127.0.0.1:15000: connect: connection refused
2022-05-02T18:59:19Z scuttle: Blocking finished, Envoy has started
Connection: gcs-german-credit read successfully
2022-05-02T18:59:20Z scuttle: Kill received: (Action: Stopping Istio with API, Reason: ISTIO_QUIT_API is set, Exit Code: 0)
2022-05-02T18:59:20Z scuttle: Stopping Istio using Istio API 'http://localhost:15020' (intended for Istio >v1.2)
2022-05-02T18:59:20Z scuttle: Received signal 'child exited', passing to child
2022-05-02T18:59:20Z scuttle: Sent quitquitquit to Istio, status code: 200
```

For more details about how to build skills go to [Cortex Fabric Documentation - Development - Develop Skills](https://cognitivescale.github.io/cortex-fabric/docs/development/define-skills)
