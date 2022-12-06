## Agent 

We expose two services in the same agent

![Predict Service](diagram_pred.png)    ![Predict Service](diagram_train.png)


```mermaid
graph LR
    subgraph MANAGED CONTENT
        predictdata[(Data to predict on)]
        traindata[(Training Data)]
        outputdata[(Predictions)]
    end
    subgraph PROFILES
        profile[(profile)]
    end
    subgraph EXPERIMENTS
        model[(model)]
    end
    subgraph INPUT SERVICE
            direction TB
            ser1((Prediction Service)) 
            ser2((Training Service)) 
        end
    subgraph EXTRACT
        extract-pred["extract-pred"]
        extract-train["extract-train"]
        profile --> extract-pred
        profile --> extract-train
    end
    subgraph PREDICT
        direction LR
        ser1 --> extract-pred
        extract-pred --> predictdata
        predictdata ---> predict
        model ---> predict
        predict --> outputdata
    end
    subgraph TRAIN
        direction LR
        ser2 --> extract-train
        extract-train ---> traindata
        traindata ---> train
        train --> model
    end
```

```mermaid
flowchart TD
    Update --> B{Does connection for Predictions exist?}
    B -->|Yes| C{Does Datasource for Predictions exist?}
    C --> |Yes| D[\Start Streaming ingestion\]
    C --> |No| H[Create Datasource for Predictions]
    H --> I[Update ProfileSchema to merge the Predictions Datasource]
    I --> D
    D --> E{Has the streaming job finished?}
    E --> |No| E
    E --> |Yes| G[\Start Profile Ingestion\]
    G --> End
    B -->|No| F[Create Streaming Connection to Managed Content]
    F --> C
```

## Deploy skills

1. build
```
cortex workspaces build
```

2. publish 
```
cortex workspaces publish
```

3. save the agent
```
cortex agents save agent.json
```

4. call training service 
```
cortex agents invoke --params-file skills/extract/invoke/request/message_train.json example3 train_service
```

5. call prediction service
```
cortex agents invoke --params-file skills/extract/invoke/request/message_pred.json example3 predict_service
```