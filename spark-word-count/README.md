### Spark Submit Job Skill

Cortex skill that submits spark jobs to a local/external cluster.

#### Prerequisites
    Maven/Gradle

### Project Structure
java source code location `src/main/java`
java source code location `src/main/python`

#### Steps 

#### 1. Submit Java based Spark job (Optional):

i. Build and package the project if it is a maven or gradle based using the respective build tool. I'm using maven to build the current project using the command as below. This step generates a jar file `spark-word-count-1.0-SNAPSHOT.jar` in the `target` folder of the project root dir.

        make mvn.build.package
  
ii. Build the base docker image(spark-base:latest) of the spark distribution by running the below cmd from the project root directory.

        make docker.build.spark-base
iii. Build the spark container image which spark uses for spinning up the containers for drivers and executors when we submit a job to a k8s cluster.
        
        make docker.build.k8s.container

iv. Modify the spark-submit command in the submit-spark-job.sh in the root dir as per your requirements. By default it submits the job to a local spark cluster when you invoke the skill. Examples of local and remote cluster spark job submissions:

        `Local`: spark-submit --class SparkWordCount --master local local:///${SPARK_HOME}/work-dir/target/spark-word-count-1.0-SNAPSHOT.jar s3a://test-mb/test/data.txt

        `k8s`: spark-submit --master k8s://https://<remote_host>:<remote_port> --deploy-mode cluster --name spark-word-count --class SparkWordCount --conf spark.executor.instances=2 --conf spark.kubernetes.container.image=svangapallycs/spark-word-count:latest --conf spark.kubernetes.authenticate.submission.caCertFile=selfsigned_certificate.pem local:///${SPARK_HOME}/work-dir/target/spark-word-count-1.0-SNAPSHOT.jar s3a://test-mb/test/data.txt

#### 2. Submit Python based Spark job (Optional):
i. Build the base docker image(spark-base:latest) of the spark distribution by running the below cmd from the project root directory.

        make docker.build.spark-base

ii. Modify the spark-submit command in the spark-submit.sh in the root dir as per your requirements. By default it submits the job to a local spark cluster when you invoke the skill. Examples of local and remote cluster spark job submissions:

        `local`: spark-submit --master local word-count.py s3a://test-mb/test/data.txt

        `k8s`: spark-submit --master k8s://https://<remote_host>:<remote_port> --deploy-mode cluster --name spark-word-count --conf spark.executor.instances=2 --conf spark.kubernetes.container.image=svangapallycs/spark-word-count:latest --conf spark.kubernetes.authenticate.submission.caCertFile=selfsigned_certificate.pem word-count.py s3a://test-mb/test/data.txt

iii. Modify the `requirements.txt` file to add packages and libraries that are needed.

#### 3. Build the docker image from project root directory
  
        make docker.build.word-count

#### 4. Push the docker image to a registry that is connected to your Kubernetes cluster and deploy action.

        make deploy.word-count
  
#### 5. Save/deploy the Skill.
  
        make skill.save
  
   The Skill is added to the Cortex Fabric catalog and is available for selection when building interventions or Agents.

   Skills that are deployed may be invoked (run) either independently or within an agent.

For more details about how to build skills go to [Cortex Fabric Documentation - Development - Develop Skills](https://cognitivescale.github.io/cortex-fabric/docs/development/define-skills)