### Cortex Experiment Train and Predict Skill

#### Train Action: 
Cortex action to train and save model in Experiment. 
This is implemented as a background job (Cortex Action of type Job) that trains a model on specified connection.
For simplicity we selected german credit dataset and training on this dataset. 
In the end trained model is saved with metadata in Cortex Experiments to fetch and run predictions later.
Every time we run this Job to train model, the model will be versioned and stored with its metadata. 
Later this metadata and version numbers will be used to select required model.

#### Predict Action: 
This Action demonstrates fetching saved model from Cortex Experiment and running predictions on it. 
For simplicity, this skill is loading the latest trained model in the Cortex Experiment. 
In production environment, users can select a model at specific version or based on metadata saved. 
This will load and cache the model in memory for subsequent prediction invocation.

Note:
>This project assumes a fast api server with endpoints /invoke and /init that is run with the Uvicorn python3 binary; 
you may change to another framework or language.

#### Files to review
* `skill.yaml` Skill definition
* `types.yaml` Types definition (used in `skill.yaml`)
* `train/train.py` Model train code to be run as Cortex Job
* `train/requirements.txt` Python3 libraries dependencies
* `train/Dockerfile` to build Docker image for train action
* `Makefile` Makefile to build and deploy skill and actions
* `predict/main.py` Predict code to be run as Cortex Daemon
* `predict/requirements.txt` Python3 libraries dependencies
* `predict/Dockerfile` to build Docker image for predict action

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
6. Save custom types from `types.yaml` file (if any).
```
cortex types save types.yaml --project <Project Name>
```

7. Modify the `skill.yaml` file.
8. Save/deploy the Skill.
  ```
  cortex skills save -y skill.yaml --project <Project Name>
  ```

   The Skill is added to the Cortex Fabric catalog and is available for selection when building interventions or Agents.

   Skills that are deployed may be invoked (run) either independently or within an agent.

### Steps to Test

Steps to test

1. Save Connection for s3 file(training data)

2. Deploy actions and skill

3. Run train job, payload:
    ```
    {"connection_name": "connection_name",
    "model_name": "german-credit-model",
    "model_source": "CS", //optional
    "model_tags": [
    {
    "label": "german-credit",
    "value": "german-credit"
    },
    {
    "label": "classification",
    "value": "classification"
    }
    ], //optional
    "model_title": "German Credit Model", //optional
    "model_type": "Classification", //required
    "model_status": "In development", //required
    "model_mode": "Single" //optional
    }
    ```
4. Note experiment names and run id from train job output

5. Run predict payload:
    ```
    {
    "exp_name": "exp_name",
    "runId": "", //optional
    "instances": []
    }
    ```


For more details about how to build skills go to [Cortex Fabric Documentation - Development - Develop Skills](https://cognitivescale.github.io/cortex-fabric/docs/development/define-skills)
