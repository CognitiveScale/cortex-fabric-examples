### Spark Submit Job(Skill)

Cortex skill that submits spark jobs to a local/external cluster.

#### Submitting Applications
The spark-submit script in Spark’s bin directory is used to launch applications on a cluster. It can use all of Spark’s supported cluster managers through a uniform interface so you don’t have to configure your application especially for each one.

Launching Applications with spark-submit
Once a user application is bundled, it can be launched using the bin/spark-submit script. This script takes care of setting up the classpath with Spark and its dependencies, and can support different cluster managers and deploy modes that Spark supports:

    ./bin/spark-submit \
      --class <main-class> \
      --master <master-url> \
      --deploy-mode <deploy-mode> \
      --conf <key>=<value> \
      ... # other options
      <application-jar> \
      [application-arguments]
Some of the commonly used options are:

    --class: The entry point for your application (e.g. org.apache.spark.examples.SparkPi)
    --master: The master URL for the cluster (e.g. spark://23.195.26.187:7077)
    --deploy-mode: Whether to deploy your driver on the worker nodes (cluster) or locally as an external client (client) (default: client) †
    --conf: Arbitrary Spark configuration property in key=value format. For values that contain spaces wrap “key=value” in quotes (as shown). Multiple configurations should be passed as separate arguments. (e.g. --conf <key>=<value> --conf <key2>=<value2>)
    application-jar: Path to a bundled jar including your application and all dependencies. The URL must be globally visible inside of your cluster, for instance, an hdfs:// path or a file:// path that is present on all nodes.
    application-arguments: Arguments passed to the main method of your main class, if any


#### Prerequisites
    Maven/Gradle

### Project Structure
java source code location `src/main/java`
java source code location `src/main/python`

#### Steps 

Build the base docker image(spark-base:latest) of the spark distribution by running the below cmd from the project root directory.

        make docker.build.spark-base

##### 1. Submit Python based Spark job (Optional):

i. Add the python dependencies in `requirements.txt` file to add packages and libraries that are needed.

ii. Build the spark container image which spark uses for spinning up the containers for drivers and executors when we submit a job to a k8s cluster.
        
        make docker.build.k8s.container
        make docker.push.k8s.container

Note: Above step is needed only if you modify the existing business logic and you need to update the base image of the spark-batch-predict Dockerfile

iii. Build the docker image from project root directory
  
        make docker.build.spark-batch-predict

##### 2. Submit Java based Spark job (Optional):

i. Build and package the project if it is a maven or gradle based using the respective build tool. I'm using maven to build the current project using the command as below. This step generates a jar file `spark-batch-predict-1.0-SNAPSHOT.jar` in the `target` folder of the project root dir.

        make mvn.build.package
   
ii. Build the spark container image which spark uses for spinning up the containers for drivers and executors when we submit a job to a k8s cluster.
        
        make docker.build.k8s.container
        make docker.push.k8s.container

Note: Above step is needed only if you modify the existing business logic and you need to update the base image of the spark-batch-predict Dockerfile

iii. Build the spark-batch-predict docker image using the below cmd:
        
        make docker.build.spark-batch-predict

##### 3. Modify the experiment metadata to update the config as below. By default it submits the job to a local spark cluster when you invoke the skill. Examples of local and remote cluster spark job submissions:

        {     
            "pyspark": {
                "pyspark_bin": "bin/spark-submit",
                "options": {
                    "--master": "k8s://<master-ip>",
                    "--deploy-mode": "cluster",
                    "--name": "spark-batch-predict",
                    "--conf": {
                        "spark.executor.instances": 2,
                        "spark.kubernetes.authenticate.driver.serviceAccountName": "default",
                        "spark.kubernetes.container.image": "c12e/spark-template:spark-container-BiyRw4",
                        "spark.kubernetes.container.image.pullSecrets": "docker-login",
                        "spark.kubernetes.namespace": "cortex",
                        "spark.kubernetes.driver.master": "master-ip",
                        "spark.executor.memory": "1g",
                        "spark.shuffle.service.enabled": "false",
                        "spark.dynamicAllocation.enabled": "false",
                        "spark.network.timeout": "300s",
                        "spark.executor.heartbeatInterval": "100s",
                        "spark.kubernetes.driver.annotation.traffic.sidecar.istio.io/inject": "false",
                        "spark.kubernetes.executor.annotation.traffic.sidecar.istio.io/inject": "false",
                        "spark.kubernetes.executor.annotation.traffic.sidecar.istio.io/excludeOutboundPorts": "7078,7079",
                        "spark.kubernetes.driver.annotation.traffic.sidecar.istio.io/excludeInboundPorts": "7078,7079"
                    }
                }
            },
            "spark_base": "c12e/spark-template"
        }
        
   ```run.log_param("config", config)```



##### 3. Push the docker image to a registry that is connected to your Kubernetes cluster and deploy action.

        make deploy.spark-batch-predict
  
##### 4. Save/deploy the Skill.
  
        make skill.save
  
  
   The Skill is added to the Cortex Fabric catalog and is available for selection when building interventions or Agents.

   Skills that are deployed may be invoked (run) either independently or within an agent.

For more details about how to build skills go to [Cortex Fabric Documentation - Development - Develop Skills](https://cognitivescale.github.io/cortex-fabric/docs/development/define-skills)