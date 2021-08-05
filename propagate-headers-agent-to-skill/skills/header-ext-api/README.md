### Add the url and path properties in skill.yaml
This is a required property for external apis. The request will me made to this url.

Example:
```
- name: url
    title: URL
    description: http[s]://host:port
    required: true
    type: String
    defaultValue: https://httpbin.org
  - name: path
    title: API path
    description: API URL Path 
    required: true
    type: String
    defaultValue: /get
```
We have used httpbin.org/get as the full URI here. It sends back all the data as response which it receives in request.

Refer: https://httpbin.org/#/HTTP_Methods/get_get

For custom requests, the headers should be accessible via `Request.headers`

### Responses
Use cortex's cli to get the activation
```
cortex agents get-activation d0f80d3c-1aea-4b81-bed8-ccfed5f57760
```

The activation response will include the headers.

Example:
```
  {
  "success": true,
  "requestId": "d0f80d3c-1aea-4b81-bed8-ccfed5f57760",
  "agentName": "agent7",
  "serviceName": "serv7",
  "sessionId": "d0f80d3c-1aea-4b81-bed8-ccfed5f57760",
  "projectId": "test-14d30",
  "username": "cortex@example.com",
  "payload": "helli",
  "headers": {
    "test-header": "test-header-value",
    "meta-transid": "20210331154525DEXL20100CM619284999",
    "meta-src-envrmt": "PROD"
  },
  "start": 1628076068719,
  "status": "COMPLETE",
  "response": {
    "args": {},
    "headers": {
      "Accept": "application/json",
      "Accept-Encoding": "gzip, deflate, br",
      "Content-Type": "application/json",
      "Host": "httpbin.org",
      "Meta-Src-Envrmt": "PROD",
      "Meta-Transid": "20210331154525DEXL20100CM619284999",
      "Test-Header": "test-header-value",
      "Testheader": "application/json",
      "User-Agent": "got (https://github.com/sindresorhus/got)",
      "X-Amzn-Trace-Id": "Root=1-610a7825-6d903cbb384c01c16c85dda0"
    },
    "origin": "49.37.76.32",
    "url": "https://httpbin.org/get"
  },
  "end": 1628076070205
}
```
