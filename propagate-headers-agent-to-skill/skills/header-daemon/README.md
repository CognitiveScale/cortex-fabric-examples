Use cortex's cli to get the activation
```
cortex agents get-activation d14bae04-2817-4f68-84a7-b71d56129d71
```
The activation response will include the headers.

Example:
```
{
  "success": true,
  "requestId": "d14bae04-2817-4f68-84a7-b71d56129d71",
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
