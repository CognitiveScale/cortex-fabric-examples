### Synchronous Agent Invocation Example

Examples to demonstrate the synchronous agent invoke capabilities

#Prerequisites
* Python 3.7
* Cortex Python SDK v6

#### File Structure:
* `src/main.py` Python code that invokes the Agent
* `src/config.json` Config.json 
* `requirements.txt` Python libraries dependencies

#### Examples:
* Using the `sync=true` flag in the Agent invocation endpoint as a query param. This may not work always as it depends on the type of Skill used downstream (like Merge)
    
  Sample Payload:
        
        {"text": "Hello, World!"}
     

For more information on Agents, please refer to cortex fabric docs: <br>
https://cognitivescale.github.io/cortex-fabric/docs/build-agents/agents