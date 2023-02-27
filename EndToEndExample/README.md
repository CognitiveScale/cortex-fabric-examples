### Cortex End to End Example Demonstrating cortex-python SDK (Train and Predict Skill)

### Prerequisites
- Python 3.x
- Docker client
- Bash shell ( Power shell TBA )
- Cortex client ( installed )
- URL/Credentials for a cortex instance
- Use `make install` to install the cortex-python SDK

#### Connections and Data Set
For simplicity we selected german credit dataset(find it in [data](data)) and training on this dataset. 
We have this dataset(csv file) in a S3 bucket.
We create a connection using the cortex-sdk and use that connection for Train Action
We are using files such as [conn.json](conn.json) to define the connection definition and [config.py](config.py) for defining the secrets like AWS_PUBLIC_KEY, S3_BUCKET, FILE_NAME etc
##### Secrets 
There are certain configs and secrets like AWS Public key and Private Key we need to create and set in the Project to successfully create the connections. [Configs](https://github.com/CognitiveScale/cortex-fabric-examples/blob/5cd95021cc6ba62315e3ee23756cb3e5a98fe301/EndToEndExample/deploy_skill.py#L31-L35)
[secrets](./deploy_skill.py#L66)

Example: If you have a secret key called `awssecret` set in your project you can use it to create connections by Using `#SECURE.awssecret` as the value to one of the parameters in the Connections Object. Similary any secret key set such as `<secret_name>` can be used as `#SECURE.<secret_name>`. This needs to be updated in [config.py](config.py) 

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
* `conn.json` Connection definition
* `config.py` Configuration file to update connection and project configurations and secrets
* `actions/train/train.py` Model train code to be run as Cortex Job
* `actions/train/requirements.txt` Python3 libraries dependencies
* `actions/train/Dockerfile` to build Docker image for train action
* `Makefile` Makefile to build and push Train and Predict Action Docker images to the specified `DOCKER_PREGISTRY_URL`
* `actions/predict/main.py` Predict code to be run as Cortex Daemon
* `actions/predict/requirements.txt` Python3 libraries dependencies
* `actions/predict/Dockerfile` to build Docker image for predict action
* `deploy_skill.py` Uses cortex-python SDK to deploy skill and actions
* `tests/test_train.json` Sample Payload for Train Skill
* `tests/test_predict.json` Sample Payload for Predict Skill

#### Steps

A Makefile is provided to do these steps. Set environment variables `DOCKER_PREGISTRY_URL` (like `<docker-registry-url>/<namespace-org>`) and `PROJECT_NAME` (Cortex Project Name) and use Makefile to build and push docker images of Train and Predict Actions.
`make deploy` will build & push Docker images for Train and Predict Actions and then deploy the skill(wrapping the actions).  
Alternatively running the cells in [cortex-python_v6_example.ipynb](cortex-python_v6_example.ipynb)
will also deploy the skills and the actions.

In order to modify the actions follow the steps below: 


1. Set environment variables `DOCKER_PREGISTRY_URL` (like `<docker-registry-url>/<namespace-org>`) and `PROJECT_NAME` (Cortex Project Name)
2. Start by modifying the `conn.json` file updating the connection definition
3. Make sure the secrets such as AWS_PUBLIC_KEY, S3_BUCKET, FILE_NAME, API_ENDPOINT and CORTEX_TOKEN  are updated in the `config.py` file
4. Modify the main executable (`main.py` by default) run by the action image's entrypoint/command to handle the action's custom logic.
5. Modify the `requirements.txt` file to provide packages or libraries that the action requires.
6. Build the docker image (uses the `main.py` file) using `make build`
7. Push the docker image to a registry that is connected to your Kubernetes cluster using `make push`
8. Update the docker image mapped to the action in [skill definition](skill.json) and save the skill using using `make save-skill`

### Steps to Test

Steps to test

```make tests```

The sample payloads are here for [Train](tests/test_train.json) action and [Predict](tests/test_predict.json) action

On successfull invocation of the skills the output should look something like this
```
{
  "success": true,
  "activationId": "e1fb856d-944f-42b2-a54e-84bdf0dfd630"
}
```
we can use the activation id to get more info into the execution using the command `cortex agents get-activation <activationId>`

The train action output would look something like this
```
{
  "success": true,
  "requestId": "bbb413d0-93dd-460d-81fe-385b23b6d2e9",
  "skillName": "e2e-example",
  "inputName": "train",
  "projectId": "dev-bikash-bdaaa",
  "username": "cortex@example.com",
  "payload": {
    "connection_name": "exp-connection",
    "model_name": "german-credit-model",
    "model_source": "CS",
    "model_tags": [
      {
        "label": "german-credit",
        "value": "german-credit"
      },
      {
        "label": "classification",
        "value": "classification"
      }
    ],
    "model_title": "German Credit Model",
    "model_type": "Classification",
    "model_status": "In development",
    "model_mode": "Single"
  },
  "sessionId": "bbb413d0-93dd-460d-81fe-385b23b6d2e9",
  "start": 1629876303108,
  "status": "COMPLETED",
  "end": 1629876323368,
  "response": "2021-08-25T07:25:04Z scuttle: Scuttle 1.3.5 starting up, pid 1\n2021-08-25T07:25:04Z scuttle: Logging is now enabled\n2021-08-25T07:25:04Z scuttle: Blocking until Envoy starts\n2021-08-25T07:25:04Z scuttle: Polling Envoy (1), error: internal_service: dial tcp 127.0.0.1:15000: connect: connection refused\n2021-08-25T07:25:05Z scuttle: Polling Envoy (2), status: Not ready yet\n2021-08-25T07:25:06Z scuttle: Blocking finished, Envoy has started\nReading connection exp-connection\n{'s3Endpoint': '', 'bucket': 'cortex-fabric-examples', 'publicKey': '', 'secretKey': '', 'uri': ''}\ngerman_credit_eval.csv\nDownloaded training data for exp-connection\nModel saved, name: german-credit-model\n<cortex.experiment.Experiment object at 0x7fd557ae2f50> dev-bikash-bdaaa\nExperiment saved, name: gc_dtree_exp run_id: a9a03kl\n<cortex.experiment.Experiment object at 0x7fd55787e050> dev-bikash-bdaaa\nExperiment saved, name: gc_logit_exp run_id: xpb03b0\n<cortex.experiment.Experiment object at 0x7fd55787e190> dev-bikash-bdaaa\nExperiment saved, name: gc_mlp_exp run_id: jn202ms\n<cortex.experiment.Experiment object at 0x7fd557bdeb90> dev-bikash-bdaaa\nExperiment saved, name: gc_svm_exp run_id: 6l108bw\n2021-08-25T07:25:18Z scuttle: Kill received: (Action: Stopping Istio with API, Reason: ISTIO_QUIT_API is set, Exit Code: 0)\n2021-08-25T07:25:18Z scuttle: Stopping Istio using Istio API 'http://localhost:15020' (intended for Istio >v1.2)\n2021-08-25T07:25:18Z scuttle: Received signal 'child exited', passing to child\n2021-08-25T07:25:18Z scuttle: Received signal 'urgent I/O condition', ignoring\n2021-08-25T07:25:18Z scuttle: Sent quitquitquit to Istio, status code: 200\n"
}
```

Note:
>Anything printed in the terminal of a job type action will be passed as a response string.

And the Predict action output will look something like this
```
{
  "success": true,
  "requestId": "3281f0b1-5d1e-40f1-b6b5-7d46f6e21149",
  "skillName": "e2e-example",
  "inputName": "predict",
  "projectId": "dev-bikash-bdaaa",
  "username": "cortex@example.com",
  "payload": {
    "instances": [
      [
        "... < 0 DM",
        6,
        "critical account/ other credits existing (not at this bank)",
        "radio/television",
        1169,
        "unknown/ no savings account",
        ".. >= 7 years",
        4,
        "male : single",
        "others - none",
        4,
        "real estate",
        "> 25 years",
        "none",
        "own",
        2,
        "skilled employee / official",
        1,
        "phone - yes, registered under the customers name",
        "foreign - yes"
      ]
    ],
    "exp_name": "gc_dtree_exp",
    "run_id": ""
  },
  "sessionId": "3281f0b1-5d1e-40f1-b6b5-7d46f6e21149",
  "start": 1629876304703,
  "status": "COMPLETE",
  "end": 1629876306252,
  "response": {
    "predictions": [
      1
    ],
    "scores": [
      [
        1,
        0
      ]
    ],
    "labels": [
      1,
      2
    ]
  }
}
```



For more details about how to build skills go to [Cortex Fabric Documentation - Development - Develop Skills](https://cognitivescale.github.io/cortex-fabric/docs/build-skills/define-skills)
