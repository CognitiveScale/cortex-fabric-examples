### Cortex End to End Example Demonstrating cortex-python SDK (Train and Predict Skill)

#### Connections and Data Set
For simplicity we selected german credit dataset and training on this dataset. 
We have this dataset(csv file) in a S3 bucket.
We create a connection using the cortex-sdk and use that connection for Train Action

#### Train Action: 
Cortex action to train and save model in Experiment. 
This is implemented as a background job (Cortex Action of type Job) that trains a model on specified connection.
It uses the connection saved in the above step

In the end trained model is saved with metadata in Cortex Experiments to fetch and run predictions later.
Every time we run this Job to train model, the model will be versioned and stored with its metadata. 
Later this metadata and version numbers will be used to select required model.

#### Predict Action: 
This Action demonstrates fetching saved model from Cortex Experiment and running predictions on it. 
For simplicity, this skill is loading the latest trained model in the Cortex Experiment(we can specify the `run_id` as well). 
In production environment, users can select a model at specific version or based on metadata saved. 
This will load and cache the model in memory for subsequent prediction invocation.

Note:
>This project assumes a fast api server with endpoints /invoke and /init that is run with the Uvicorn python3 binary; 
you may change to another framework or language.

#### Files to review
* `skill.json` Skill definition and action mapping
* `actions/train/train.py` Model train code to be run as Cortex Job
* `actions/train/requirements.txt` Python3 libraries dependencies
* `actions/train/Dockerfile` to build Docker image for train action
* `Makefile` Makefile to build and push Train and Predict Action Docker images to the specified `DOCKER_PREGISTRY_URL`
* `actions/predict/main.py` Predict code to be run as Cortex Daemon
* `actions/predict/requirements.txt` Python3 libraries dependencies
* `actions/predict/Dockerfile` to build Docker image for predict action

#### Steps

A Makefile is provided to do these steps. Set environment variables `DOCKER_PREGISTRY_URL` (like `<docker-registry-url>/<namespace-org>`) and `PROJECT_NAME` (Cortex Project Name) and use Makefile to build and push docker images of Train and Predict Actions.
`make all` will build & push Docker images for Train and Predict Actions.  

The [cortex-python_v6_example.ipynb](cortex-python_v6_example.ipynb) contains the steps to create a connection (which is used in the actions) using the cortex-python SDK and deploy the skill and 
actions. Running the Notebook and following the steps in it will suffice.

In order to modify the actions follow the steps below: 

1. Modify the main executable (`main.py` by default) run by the action image's entrypoint/command to handle the action's custom logic.
2. Modify the `requirements.txt` file to provide packages or libraries that the action requires.
3. Build the docker image (uses the `main.py` file)
  ```
  make build
  ```
4. Push the docker image to a registry that is connected to your Kubernetes cluster.
  ```
  make push
  ```
6. Modify the `skill.json` file.

### Steps to Test

Steps to test

```make tests```


For more details about how to build skills go to [Cortex Fabric Documentation - Development - Develop Skills](https://cognitivescale.github.io/cortex-fabric/docs/build-skills/define-skills)
