Use cortex's cli to get the activation
```
cortex agents get-activation 1b446da8-7f52-43e4-b4eb-6901265d6439
```
The activation response will include the headers.

Example:
```
{
  "success": true,
  "requestId": "1b446da8-7f52-43e4-b4eb-6901265d6439",
  "agentName": "agent8",
  "serviceName": "serv8",
  "sessionId": "1b446da8-7f52-43e4-b4eb-6901265d6439",
  "projectId": "test-14d30",
  "username": "cortex@example.com",
  "payload": "helli",
  "headers": {
    "test-header": "test-header-value",
    "meta-transid": "20210331154525DEXL20100CM619284999",
    "meta-src-envrmt": "PROD",
    "content-type": "application/json"
  },
  "start": 1628080332389,
  "status": "COMPLETE",
  "response": "Received: {'activationId': '1b446da8-7f52-43e4-b4eb-6901265d6439', 'sessionId': '1b446da8-7f52-43e4-b4eb-6901265d6439', 'channelId': '40490593-1ee9-47a0-85ff-0b36a93e2ab3', 'projectId': 'test-14d30', 'agentName': 'test-14d30-agent8', 'skillName': 'test-14d30-header-job', 'timestamp': 1628080332389, 'token': 'eyJraWQiOiJLNVNhLVBYZ1o4VWt1XzRUbU5LZUp3bE1CZmI1MWNhZWZRY2QzUTY1dzRZIiwiYWxnIjoiRWREU0EifQ.eyJiZWFyZXIiOiJ1c2VyIiwiaWF0IjoxNjI4MDY2NjM0LCJleHAiOjE2MjgxNTMwMzMsInJvbGVzIjpbImNvcnRleC1hZG1pbnMiXSwic3ViIjoiY29ydGV4QGV4YW1wbGUuY29tIiwiYXVkIjoiY29ydGV4IiwiaXNzIjoiY29nbml0aXZlc2NhbGUuY29tIn0.VHKeq9vJNWf0PLk3eitjtIAhbgKhl00TuIX5WNXf4q7W94LqT6y76wOQPOxvVELpsN3BPe0vMIut4zsw8NXiDw', 'payload': 'helli', 'apiEndpoint': 'http://cortex-internal.cortex.svc.cluster.local', 'properties': {}, 'outputName': 'result', 'headers': {'test-header': 'test-header-value', 'meta-transid': '20210331154525DEXL20100CM619284999', 'meta-src-envrmt': 'PROD', 'content-type': 'application/json'}}\n",
  "end": 1628080352603
}

```
