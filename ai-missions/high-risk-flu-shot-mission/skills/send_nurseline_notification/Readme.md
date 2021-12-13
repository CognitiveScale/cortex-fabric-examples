### Send Nurseline Notification


#### Description:
For the Profile Ids derived from the configured Cohort, notifications are sent on the preferred communication channel that request that members call the nurse-line for more information about scheduling a flu shot with an available flu shot provider.

#### Invoke Skill From Cli

```
cortex skills invoke hw/nursline  request  --params '{"payload": {"profiles": ["41993197", "66041768"]}}' --project ${PROJECT_NAME}
```

#### Action API: 
<Action API Placeholder>

#### Input:
List of Profile ids

```
{"profiles": ["41993197", "66041768"]}
```

#### Ouput:
Acknowledgement of skill status
```
{
  "success": true,
  "payload": {
    "message": "Successfully sent notifications to given profile IDs"
  },
  "activationId": "df73df5a-27e7-4b57-aff1-7a5ec14238a3",
  "elapsedTime": 6
}
```