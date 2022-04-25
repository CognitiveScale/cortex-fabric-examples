# Kafka client example

The kafka client requires compilation, on some platforms it is recommended to use Miniconda3 to use precompiled binaries.

## Requirements. 
* Python3.x or Miniconda3
* Fabric cluster
* Kafka instance

## Installing 

Using python3
```
virtualenv venv
source venv/bin/activate
pip install -r requirements.txt
```

Using Miniconda3
```
conda create -p ./conda
conda activate ./conda
conda install -c conda-forge python-confluent-kafka pip
pip install cuid
```

## Running 

```
python main.py -b 192.168.39.174:31945 -g kafka-example -i fabric-out -o fabric-in -n 1 --headers "{\"authorization\":\"bearer ${cortex configure token}\",\"something\":\"somevalue\"}"
```

## Strimzi kafka operator

We've tested the fabric kafka consumer/producer using the [Strimzi kafka operator](https://strimzi.io).
The operator greatly simplifies the installation and administration of a kafka cluster.

### Install the strimzi operator

```
kubectl create namespace kafka
helm repo add strimzi https://strimzi.io/charts/
helm install -n kafka kafka strimzi/strimzi-kafka-operator
```

### Create cluster

The Kafaka resource below create a pretty minial kafka cluster.
There is no replication 

```
cat << EOF | kubectl create -n kafka -f -
apiVersion: kafka.strimzi.io/v1beta2
kind: Kafka
metadata:
  name: fabric-cluster
spec:
  kafka:
    replicas: 1
    listeners:
      - name: plain
        port: 9092
        type: internal
        tls: false
      - name: tls
        port: 9093
        type: internal
        tls: true
        authentication:
          type: tls
      - name: external
        port: 9094
        type: nodeport
        tls: false
    storage:
      type: jbod
      volumes:
      - id: 0
        type: persistent-claim
        size: 10Gi
        deleteClaim: false
    config:
      offsets.topic.replication.factor: 1
      transaction.state.log.replication.factor: 1
      transaction.state.log.min.isr: 1
  zookeeper:
    replicas: 1
    storage:
      type: persistent-claim
      size: 10Gi
      deleteClaim: false
  entityOperator:
    topicOperator: {}
    userOperator: {}
EOF
```
### Create fabric in/out topics

Depending on the kafka cluster configuration topics may be auto created. 
The following kubernetes resources set a message retention message period of 1 hour.  
This is a good setting to use during development/testing

```
cat << EOF | kubectl create -n kafka -f -        
apiVersion: kafka.strimzi.io/v1beta2
kind: KafkaTopic
metadata:
  name: fabric-in
  labels:
    strimzi.io/cluster: "fabric-cluster"
spec:
  config:
    retention.ms: 3600000
  partitions: 3
  replicas: 1
EOF

cat << EOF | kubectl create -n kafka -f -        
apiVersion: kafka.strimzi.io/v1beta2
kind: KafkaTopic
metadata:
  name: fabric-out
  labels:
    strimzi.io/cluster: "fabric-cluster"
spec:
  config:
    retention.ms: 3600000
  partitions: 3
  replicas: 1
EOF

cat << EOF | kubectl create -n kafka -f -        
apiVersion: kafka.strimzi.io/v1beta2
kind: KafkaTopic
metadata:
  name: fabric-err
  labels:
    strimzi.io/cluster: "fabric-cluster"
spec:
  config:
    retention.ms: 3600000
  partitions: 3
  replicas: 1
EOF

```

### Create gateway config

`kubectl create -n cortex -f k8s/gateway-connector-configs.yaml`

### Testing

Shell into the kafka pod and run the following
```
./bin/kafka-console-producer.sh --bootstrap-server localhost:9092 --topic fabric-in
```

Sample request for "default" deployment where kafka invokes any agent

```json
{"correlationId": "i2" ,"agentName": "cortex/hello_agent", "projectId": "johan", "serviceName": "input", "payload": {"text": "test message"}}
`````
{"`agentName`": "cortex/hello_agent", "projectId": "johan", "serviceName": "input", "payload": {"text": "test message"}}

{"agentName": "cortex/hello_agent", "projectId": "sssjohan", "serviceName": "input", "payload": {"text": "test message"}}

To see output run the following
```
./bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --from-beginning --topic fabric-out
```

**Testing outside of the cluster**
Obtain the external bootstrap ip and port (tested with minikube)
```
echo "$(minikube ip):$(kubectl -n istio-system get service fabric-cluster-kafka-external-bootstrap --namespace kafka -o jsonpath='{.spec.ports[0].nodePort}')"
```

Use this address to access the cluster remotely 

python main.py -b 192.168.39.174:31945 -g johangw -i fabric-out -o fabric-in -n 1 --headers "{\"authorization\":\"bearer ${cortex configure token}\",\"something\":\"somevalue\"}"