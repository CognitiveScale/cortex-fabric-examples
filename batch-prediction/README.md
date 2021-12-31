### Batch Prediction Template Skill

Cortex Batch Prediction Skill that runs a background job which makes predictions in batches using the model.

### Requirements:
- cortex-cli 2.0.x
- Docker

#### File Structure:
* `skill.yaml` Skill definition
* `predict/main.py` Python3 code that gets executed in the job
* `train/main.py` Example Python3 code for training the model
* `requirements.txt` Python3 libraries dependencies
* `Dockerfile` to build Docker image for this skill

### Skill Properties:
* `batch-size`: Number of Records to be processed in one batch
* `connection-name`: Connection used to read and write data
* `experiment-name`: Experiment name to retrieve Models
* `run-id`: Run id of the experiment
* `outcome`: Prediction class or label in the dataset
* `output-path`: Output S3 path to save the predictions
* `output-collection`: Output Mongo collection to save the predictions

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

* Model Pickle Structure with encoder:
    
        {"model": d_tree_model, "cat_columns": categorical_columns, "encoder": encoder, "normalizer": normalizer}

* Model Pickle Structure w/o encoder: 
                    
        We directly pass the trained model object here after model.fit()
        example: DecisionTreeClassifier()

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

1. Modify the `requirements.txt` file to provide packages or libraries that the action requires if any.
2. Build the docker image (uses the `main.py` file)
  
        make build
 
3. Push the docker image to a registry that is connected to your Kubernetes cluster.
  
        make push
  
4. Deploy the action and save skill.
  
        make deploy
  
5. Sample Skill Invocation Input: `{}`

6. An output file will be generated at the output path provided in properties once the predictions are done.

* Note: Refer to `train/main.py`  for training example used for this skill.
   
   The Skill is added to the Cortex Fabric catalog and is available for selection when building interventions or Agents.

   Skills that are deployed may be invoked (run) either independently or within an agent.

For more details about how to build skills go to [Cortex Fabric Documentation - Development - Develop Skills](https://cognitivescale.github.io/cortex-fabric/docs/development/define-skills)
