### Spark Submit Job(Skill)

Cortex skill that submits spark jobs to an external EMR cluster.

### Prerequisites

- Python 3.x
- Docker client
- Cortex client ( installed )
- URL/Credentials for a cortex instance
- EMR 6.X setup (EMR on EC2) and neccesary IAM roles setup for s3 and docker registry(either ECR or docker hub setup)
- URL/Credentials for a cortex instance

### Project Structure
job source code location `emr-container-image/src/job.py`
job submit code location `submit-job.py`

#### Steps
Update config.json with registry to push spark base & container images. In the current example, we are pushing to 610527985415.dkr.ecr.us-east-1.amazonaws.com/emr-spark-template (an ECR registry, this can be a dockerhub repo as well, EMR needs to have access to this repo).
        
        "spark_base": "610527985415.dkr.ecr.us-east-1.amazonaws.com/emr-spark-template"

Builds & Pushes all the images and you can skip the below 5 steps upto save types:

        make deploy.all
(make sure you have push access to registries in use in case of ECR
 `aws ecr get-login-password --region region | docker login --username AWS --password-stdin aws_account_id.dkr.ecr.region.amazonaws.com` or `cortex docker login`
)

The `make deploy.all` updates `config.json` and generates `config.pickle` file(this is the spark config). We need to upload the the model and the config pickle file to the experiment (run-id) that we want to use. (For this example we can use the same model in [batch-predcition](https://github.com/CognitiveScale/cortex-fabric-examples/tree/master/batch-prediction/model) example)

        ```
        cortex experiments upload-artifact <experiment_name> <run_id> german_credit_model.pickle model --project <project_name>
        cortex experiments upload-artifact <experiment_name> <run_id> config.pickle spark-config --project <project_name>
        ```

Setup the skill in an agent and invoke or use `make tests` after updating payload.json in [tests](/tests)
        ```
        {
            "payload":{
                "input":null
                },
            "properties":{
                "cluster-id":"j-2F41SXO8SHBXO", 
                "experiment-name":"gc_dtree_exp",
                "run-id":"so60yh3",
                "aws-secret":"",
                "s3-bucket":"",
                "aws-access-id":"",
                "input-file":"s3://test-smr-remote/german_credit_eval.csv",
                "output-path":"s3://test-smr-remote/out-test",
                "outcome":"outcome"
            }
        }
        ```

Skill properties: There are certain skill properties that need to be provide for the skill to work
Descriptions:
1. EMR Cluster id - The EMR Cluster Id we want to submit the job to
2. Experiment Name - The Experiment name for the model
3. Run ID - The Run ID for the Experiment
4. AWS Secret - The AWS Secret for an IAM that has EMR and S3 access
5. AWS Access Key Id - The AWS Access key for the above secret
6. S3 input path - The path for the input file
7. S3 output path - The path where the predictions will be saved
8. Prediction class or label - The label to be predicted

### About
There are essentially 2 main ways to create an EMR cluster

- EMR on EC2
- EMR on EKS

Both of these cluster types come with docker container support, but we need to pass correct configurations and docker installation scripts (for installing docker- look at `install-docker.sh`) to activate Docker support for YarnNodeManager while launching the Cluster.(steps for deploying a cluster added below)

Q: What do we mean by Docker Support for YarnNodeManager?
A: Amazon EMR 6.x supports Hadoop 3, which allows the YARN NodeManager to launch containers either directly on the Amazon EMR cluster or inside a Docker container. 

Q: Why do we neeed Docker Container Support?
A: Docker containers provide custom execution environments in which application code runs. The custom execution environment is isolated from the execution environment of the YARN NodeManager and other applications. Also helps us package the dependencies in a better way

#### EMR on EC2

Installation steps :

Deployment steps are comprehensively detailed in a step wise manner in the blog [here](https://hangar.tech/posts/emr-docker/) the gist of which are (these can be considered pre requisites for us, to be able to submit jobs):

Creating a file called install-docker.sh and copy the following into it:
	```
	#!/bin/sh

    sudo yum install -y docker    # Install docker
    sudo systemctl start docker   # Start the docker service
    ```

Which we upload to an s3 bucket s3://test-smr-remote/install-docker.sh. We use this as part of bootstrap actions, commands that are run before the cluster starts running any steps
The following configuration needs to be passed while setting up the cluster to setup the trusted docker registries that Yarn is going to use to pull the image from 
	```
        [
            {
                "classification": "container-executor",
                "configurations": [
                    {
                        "classification": "docker",
                        "properties": {
                            "docker.trusted.registries": "local,centos,<account>.dkr.ecs.<region>.amazonaws.com"
                        }
                    }
                ]
            }
        ]
    ```




[Refer1](https://docs.aws.amazon.com/emr/latest/ReleaseGuide/emr-spark-docker.html)
[Refer1](https://docs.aws.amazon.com/emr/latest/ManagementGuide/emr-gs.html)

For more details about how to build skills go to [Cortex Fabric Documentation - Development - Develop Skills](https://cognitivescale.github.io/cortex-fabric/docs/development/define-skills)