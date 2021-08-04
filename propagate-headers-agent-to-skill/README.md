# Propagate header from agent to skill example (cortex/propagate_headers_to_skill_on_agent_invoke)
The `cortex/propogate_headers_to_skill_on_agent_invoke` agent consists of a single skill `cortex/propagate-header-skill`.
- `cortex/propagate-header-skill` responds back with the whitelisted request headers.  

## Requirements
- Python 3.x
- Docker client
- Bash shell ( Power shell TBA )
- Cortex client
- URL/Credentials for a cortex instance


## Whitelisting the headers
Using the cli, add a new property in the agent definition, with the name: 'allowed-headers' and values with the header names to be whitelisted(comma-separated)
Example:
```{         
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
The activation response will include the headers.
```
{
  "success": true,
  "requestId": "632a8421-7df8-4770-b6b0-57b513f2e467",
  "agentName": "agent-invoke-h-58584",
  "serviceName": "service-header-propagate",
  "sessionId": "632a8421-7df8-4770-b6b0-57b513f2e467",
  "projectId": "aditya-template",
  "username": "cortex@example.com",
  "headers": {
    "test-header": "test-header-value",
    "meta-transid": "20210331154525DEXL20100CM619284999",
    "meta-src-envrmt": "PROD"
  },
  "start": 1628057522309,
  "status": "COMPLETE",
  "response": {
    "host": "aditya-template-propagate-header-skill-propagate-header-skill.cortex.svc.cluster.local:5000",
    "user-agent": "got (https://github.com/sindresorhus/got)",
    "skill-yaml-header": "skill-yaml-header-val",
    "test-header": "test-header-value",
    "meta-transid": "20210331154525DEXL20100CM619284999",
    "meta-src-envrmt": "PROD",
    "content-type": "application/json",
    "accept": "application/json",
    "content-length": "1059",
    "accept-encoding": "gzip, deflate, br",
    "x-forwarded-proto": "http",
    "x-request-id": "d352840e-d785-4cb2-af75-08e681e43e0f",
    "x-envoy-attempt-count": "1",
    "x-forwarded-client-cert": "By=spiffe://cluster.local/ns/cortex/sa/default;Hash=08b25decbfe63b2dc48857ca39c05c446074ddd83958f0bd7d5f8e5c62d4ef42;Subject=\"\";URI=spiffe://cluster.local/ns/cortex/sa/default",
    "x-b3-traceid": "90c6238d175ba47a9cd0fe5c13a26ef6",
    "x-b3-spanid": "f64e37c8c9b35b9b",
    "x-b3-parentspanid": "9cd0fe5c13a26ef6",
    "x-b3-sampled": "0"
  },
  "end": 1628057522372
}
```
Note that the response has request headers and skill yaml headers. There would be few more headers which are added by default by the external service used to invoke the agent.