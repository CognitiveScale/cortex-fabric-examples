### Send Schedule Appointment Notification


#### Description:
For the Profile IDs derived from the configured Cohort, notifications are sent on the preferred communication channel that request that members schedule a telehealth appointment.


#### Invoke Skill From Cli

```
cortex skills invoke hw/sch-appointment  request  --params '{"payload": {"profiles": ["31993127", "36041761"]}}' --project ${PROJECT_NAME}
```

#### Action API: 
<Action API Placeholder>

#### Input:
List of Profile ids

```
{"profiles": ["31993127", "36041761"]}
```


#### Output:
Acknowledgement of skill status
```
{
  "success": true,
  "payload": {
    "message": "Successfully sent notifications to given profile IDs"
  },
  "activationId": "3f73df5a-27e7-4b57-aff1-3a5ec14238a5",
  "elapsedTime": 6
}
```