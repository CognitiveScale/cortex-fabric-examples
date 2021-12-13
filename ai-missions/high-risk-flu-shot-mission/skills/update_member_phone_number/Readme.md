### Update member phone number

#### Description:
For the Profile IDs derived from the configured Cohort, requests are sent to members to update their phone numbers.

#### Invoke Skill From Cli

```
cortex skills invoke hw/update-phone  request  --params '{"payload": {"profiles": ["41993296", "46041761"]}}' --project ${PROJECT_NAME}
```

#### Action API: 
<Action API Placeholder>

#### Input:
List of Profile ids

```
{"profiles": ["41993296", "46041761"]}
```


#### Ouput:
Acknowledgement of skill status
```
{
  "success": true,
  "payload": {
    "message": "Successfully sent 'update phone number' request to given profile IDs"
  },
  "activationId": "4f73df5a-27e7-4b57-aff1-4a5ec14228a1",
  "elapsedTime": 6
}
```

