### Send Notification Via Sydneycare 

#### Description:
For the given list of profile IDs, this skill sends flu-shot reminder notifications on Sydneycare app along with nearby flu-shot provider details based on zipcode of member.

#### Invoke Skill From Cli

```
cortex skills invoke hw/sydneycare  request  --params '{"payload": {"profiles": ["41993197", "66041768"]}}' --project ${PROJECT_NAME}
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