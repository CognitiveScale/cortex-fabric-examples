# Spark-submit jobs

This page contains instructions for working with Spark Submit jobs.

Spark-submit is an Apache application that Cortex job Skills use to orchestrate data ingestion with improved performance.

A Spark-submit job is packaged in a Cortex Skill. The Skill is added to the Cortex Fabric catalog and used as a template. The Skill is then available for selection when building Interventions or Agents. Skills that are deployed may be invoked either independently or within an Agent.

## Submitting Applications

The Spark-submit script in Spark’s bin directory is used to launch applications on a cluster. It has access to all of Spark’s supported cluster managers through a uniform interface. This means you do not have to configure your application for each job.

## Launching Applications with Spark Submit

Once an application is bundled, it can be launched using the Spark Submit script as seen in the example below.

This script:

- Sets up the classpath with Spark and its dependancies.
- Supports different cluster managers.
- Deploys modes that Spark supports.

Example:
```
./bin/spark-submit \
--class <main-class> \
--master <master-url> \
--deploy-mode <deploy-mode> \
--conf <key>=<value> \
<application-jar> \
[application-arguments]
```

Some of the commonly used options are:

| Option | Description | Example | Default |
| ------ | ----------- | ------- | ------- |
| `--class` | The entry point for your application. | `org.apache.spark.examples.SparkPi` | |
| `--master` | The master URL for the cluster. | `spark://23.195.26.187:7077` | |
| `--deploy-mode` | Used to deploy your driver on the worker nodes (cluster) or locally as an external client. | `client` | `client` |
| `--conf` | Arbitrary Spark configuration property in key=value format. For values that contain spaces wrap “key=value” in quotes. Multiple configurations should be passed as separate arguments. | `--conf <key>=<value> --conf <key2>=<value2>` | |
| `<application-jar>` | Path to a bundled jar including your application and all dependencies. The URL must be globally visible inside of your cluster. | a `hdfs://` path or a `file://` path that is present on all nodes | |
| `application-arguments` | Arguments passed to the main method of your main class, if any. | |

## Use Spark-submit with Cortex

### Prerequisites
Maven/Gradle installed.

### Project Structure
[To view project structure, view python source code location](https://github.com/CognitiveScale/cortex-fabric-examples/tree/master/spark-batch-prediction/src/main/python).

### Build Base Image

1. Update the `config.json` file with the registry URL where you are pushing the Spark base and container images. In the example, images are being pushed to `c12e/spark-template`.
  ```
  "spark_base": "c12e/spark-template"
  ```
2. Update `deploy-job.sh` with Docker registry details and run the following commands:

  | Command | Description |
  | ------- | ----------- |
  | `REGISTRY=<private-registry>` | Private registry without 'https//' |
  | `REGISTRY_PASS=<password>` | Private Registry Password |
  | `REGISTRY_USER=<user>` | Private registry Username for external registries |

3. Build the base Docker image (`spark-base:latest`) of the Spark distribution by running the following command from the project root directory.
  ```
  make docker.build.spark-base
  ```

<Alert title="NOTE" color="primary">

To build and push all the images at once, run the following command before step 3.
```
make deploy.all
```

</Alert>

### Create Config Pickle File

1. Submit Python-based Spark job (optional):

    - a. Add the the packages and libraries that are needed to the `requirements.txt` file.

    - b. Build the Spark container image that Spark uses to spin up the containers for drivers and executors when a job is submitted in a K8s cluster.
      ```
      make docker.build.k8s.container
      ```
      ```
      make docker.push.k8s.container
      ```

      <Alert title="NOTE" color="primary">

      The above step is only needed if you modify the existing business logic and need to update the base image of the `spark-batch-predict` Dockerfile.

      </Alert>

    - c. Build the Docker image from project root directory.
      ```
      make docker.build.spark-batch-predict
      ```
2. Push/publish the Docker image to a registry that is connected to your Kubernetes cluster.
  ```
  make deploy.spark-batch-predict
  ```
3. Save/deploy the Skill.
  ```
  make skill.save
  ```
4. Save the types for input/output.
  ```
  make types.save
  ```
5. The K8s cluster administrator must provide access to Spark to create pods for executors when running on K8s.
  ```
  kubectl apply -f spark-rbac.yaml
  ```
6. Update the options in the `config.json` file, and upload the file as an experiment artifact.
  * `--master` - Cluster Master IP. You can get it by running `kubectl cluster-info`.
  * `spark.kubernetes.driver.master` - Same as above without `k8s://`.
  * `spark.kubernetes.container.image.pullSecrets` - K8s command for pulling the docker image.

      ```
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
      ```

Output: A pickle file is created.
```
config = json.load(open('config.json', 'rb'))
pickle.dump(config, open('config.pickle', 'wb'))
```

[To read more about accessing Spark UI during job run, go here](https://spark.apache.org/docs/latest/running-on-kubernetes.html#accessing-driver-ui).

For more details about how to build Skills go to [Cortex Fabric Documentation - Define Skills](/build-skills/define-skills.md).
