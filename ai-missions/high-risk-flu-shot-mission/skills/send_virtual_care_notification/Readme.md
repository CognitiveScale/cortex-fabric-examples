### Send Virtualcare Notification


#### Description:
For the Profile IDs derived from the configured Cohort, member contact information is shared with virtual care nurses, who call members to provide information and schedule flu-shots.

#### Invoke Skill From Cli

```
cortex skills invoke hw/virtual-care  request  --params '{"payload": {"profiles": ["41993296", "46041761"]}}' --project ${PROJECT_NAME}
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
    "message": "Successfully sent given profile details to virtual care nurse"
  },
  "activationId": "4f73df5a-27e7-4b57-aff1-4a5ec14228a1",
  "elapsedTime": 6
}
```