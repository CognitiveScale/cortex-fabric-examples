### Online Prediction Skill

Long running Cortex skill serving REST API prediction requests

Note:
> This project assumes a `fast api server` with one endpoint `/invoke` that is run with the Uvicorn python3 binary; you may change to another framework or language.

### Requirements:
- cortex-cli 2.0.x
- Docker

#### Files
* `skill.yaml` Skill definition
* `predict/main.py` Python3 code serving the daemon API
* `train/main.py` Example Python3 code for training the model
* `requirements.txt` Python3 libraries dependencies
* `Dockerfile` to build Docker image for this skill

#### Skill Properties
* `daemon.method` HTTP method supported by endpoint
* `daemon.path` HTTP endpoint path in container
* `daemon.port` port on which app will be running
* `experiment-name` Experiment name to retrieve experiment data
* `run-id` ID of the experiment run
*  `model-artifact` Artifact key of the uploaded model

### Steps to Run
1. Update `model.json`, `experiment.json`, `run.json` files in `model` folder. 
2. Make sure `german_credit_model.pickle` file exists in `model` folder. 
If not create one by invoking 

       cd train
       python main.py
       cd ..

3. Create Model and Experiment:
```shell
make init
cortex experiments upload-artifact <experiment-name> <run-id> model/german_credit_model.pickle <artifact-key> --project <PROJECT_NAME>
```

4. Publish model:

       cortex models publish <model-name> --project <PROJECT_NAME>

Note:
> In case the model.pickle is updated we need to invoke `/init` endpoint to reflect the new model or, restart the skill.

#### Steps to build and deploy

Set environment variables `DOCKER_PREGISTRY_URL` (like <docker-registry-url>/<namespace-org>) and `PROJECT_NAME` (Cortex Project Name), and use build scripts to build and deploy.

Configure Docker auth to the private registry:
1. For Cortex DCI with Docker registry installed use `cortex docker login`
2. For external Docker registries like Google Cloud's GCR etc use their respective CLI for Docker login

##### On *nix systems
A Makefile is provided to do these steps.
* `export DOCKER_PREGISTRY_URL=<docker-registry-url>/<namespace-org>`
* `export PROJECT_NAME=<cortex-project>`
* `make all` will build and push Docker image, deploy Cortex Action and Skill, and then invoke Skill to test.

##### On Windows systems
A `make.bat` batch file is provided to do these steps.
* `set DOCKER_PREGISTRY_URL=<docker-registry-url>/<namespace-org>`
* `set PROJECT_NAME=<cortex-project>`
  > Below commands will build and push Docker image, deploy Cortex Action and Skill, and then invoke Skill to test.
* `make build`
* `make push`
* `make deploy`

##### Follow the below steps for deploying the skill manually.

1. Modify the `requirements.txt` file to provide packages or libraries that the action requires.
2. Build the docker image (uses the `main.py` file)
  
        make build
 
3. Push the docker image to a registry that is connected to your Kubernetes cluster.
  
        make push
  
4. Deploy the action and save skill.
  
        make deploy
  
5. Sample Skill Invocation Input
    
       {
          "columns": ["checkingstatus", "duration", "history", "purpose", "amount", "savings", "employ", "installment", "status", "others", "residence", "property", "age", "otherplans", "housing", "cards", "job", "liable", "telephone", "foreign"],
          "instances": [
               ["... >= 200 DM / salary assignments for at least 1 year", 6,"critical account/ other credits existing (not at this bank)", "car (new)", 1343, "... < 100 DM", ".. >= 7 years", 1, "male : single", "others - none", 4, "real estate", "> 25 years", "none", "own", 2, "skilled employee / official", 2, "phone - none", "foreign - no"],
               ["... < 0 DM", 28, "existing credits paid back duly till now", "car (new)", 4006, "... < 100 DM", "1 <= ... < 4 years", 3, "male : single", "others - none", 2, "car or other, not in attribute 6", "> 25 years", "none", "own", 1, "unskilled - resident", 1, "phone - none", "foreign - yes"]]
          
       }
       
6. Sample Output

       [
        1,
        2
       ]

* Note: Refer to `train/main.py`  for training example used for this skill.
   
   The Skill is added to the Cortex Fabric catalog and is available for selection when building interventions or Agents.

   Skills that are deployed may be invoked (run) either independently or within an agent.

For more details about how to build skills go to [Cortex Fabric Documentation - Development - Develop Skills](https://cognitivescale.github.io/cortex-fabric/docs/models/mlops#skill-builder)
