# Propagate header from agent to skill example (propagate_headers_to_skill_on_agent_invoke)
The `propogate_headers_to_skill_on_agent_invoke` agent consists of a single skill `propagate-header-skill`.
- `propagate-header-skill` responds back with the whitelisted request headers.

The `agent-chained-skills` agent consists of three skills in the skills folder `skill-1`, `skill-2` and `skill-3`.
- The agent has the skills are chained together from `skill-1 -> skill-2 -> skill-3`, and they demonstrate the headers used in the agent invoke are passed down to skill-3.

## Requirements
- Python 3.x
- Docker client
- Bash shell ( Power shell TBA )
- Cortex client
- URL/Credentials for a cortex instance
- jq (tool for parsing/scripting with JSON files)

## Whitelisting the headers
Using the cli, add a new property in the agent definition, with the name: 'allowed-headers' and values with the header names to be whitelisted(comma-separated)

Example:
```
{         
"name" : "allowedHeaders",
"value" : "test-header,meta-transid,meta-src-envrmt"
}
```

## Adding headers in the skill.yaml
The headers added in the skill.yaml need not be whitelisted and will be appended to the request headers for agent invoke.
Add a header property in skill.yaml with name starting with 'headers.'

Example:
```
- name: headers.skill-yaml-header
    title: skill header
    description: A sample skill yaml header
    required: true
    type: String
    defaultValue: skill-yaml-header-val
```
In case of a name match between skill.yaml header and agent invoke request header, the agent invoke header takes precedence.

## Invoking the agent
After deploying the agent and skills you can invoke the agent using any external system with the whitelisted headers. 
Headers in the http request not whitelisted will be ignored.
Here is a curl request for a sample agent invoke
```
curl --location --request POST 'https://${baseUrl}/fabric/v4/projects/:projectName/agentinvoke/:agentName/services/:serviceName' \
--header 'Authorization: Bearer ${authToken}' \
--header 'test-header: test-header-value' \
--header 'meta-transid: 20210331154525DEXL20100CM619284999' \
--header 'meta-src-envrmt: PROD' \
--header 'another-non-whitelisted-header: test-header-value' \
--header 'Content-Type: application/json' \
--data-raw '{"payload": "text"}'

```

You'll get a response with an `activationId` like below:
```
{
  "success": true,
  "activationId": "d14bae04-2817-4f68-84a7-b71d56129d71"
}
```

Use cortex's cli to get the activation
```
cortex agents get-activation d14bae04-2817-4f68-84a7-b71d56129d71
```
The sample responses for daemons, jobs and external-apis are shown in the respective skills.

Note that the response has request headers and skill yaml headers. There would be few more headers which are added by default by the external service used to invoke the agent.