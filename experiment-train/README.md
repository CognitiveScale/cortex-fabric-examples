### Cortex Experiment Train SKill

Cortex Skill to train and save model in Experiment. This is implemented as a background job (Cortex Action of type Job) that trains a model on specified dataset.
For simplicity we selected scikit iris dataset and training a RandomForestClassifier on this dataset. In the end trained model is saved with metadata in Cortex Experiments to fetch and run predictions later.
Every time we run this Job to train model, the model will be versioned and stored with its metadata. Later this metadata and version numbers will be used to select required model.

#### Files to review
* `skill.yaml` Skill definition
* `main.py` Model train code to be run as Cortex Job
* `requirements.txt` Python3 libraries dependencies
* `Dockerfile` to build Docker image for this skill

#### Steps

A Makefile is provided to do these steps. Set environment variables `DOCKER_PREGISTRY_URL` (like <docker-registry-url>/<namespace-org>) and `PROJECT_NAME` (Cortex Project Name) and use Makefile to deploy skill.
`make all` will build & push Docker image, deploy Cortex Action and Skill, and then invoke skill to test.  

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

  **(Optionally) Re-tag an image**
  ```
  docker tag <existing-image-name>:<existing-version> <new-image-name>:<new-version>
  ```
5. Deploy the action.
  ```
  cortex actions deploy --actionName <SKILL_NAME> \
  --actionType job \
  --docker <DOCKER_IMAGE> \
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
