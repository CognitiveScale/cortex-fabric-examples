### Send Sydneycare Chatbot Notification

#### Description:
For the Profile IDs derived from the configured Cohort, notifications are sent to members instructing them to engage with a chatbot in the SydneyCare app to discuss flu shot options.

#### Invoke Skill From Cli

```
cortex skills invoke hw/sc-chatbot request  --params '{"payload": {"profiles": ["42993194", "56041763"]}}' --project ${PROJECT_NAME}
```

#### Action API: 
<Action API Placeholder>

#### Input:
List of Profile ids

```
{"profiles": ["42993194", "56041763"]}
```

#### Ouput:
Acknowledgement of skill status

```
{
  "success": true,
  "payload": {
    "message": "Successfully sent notifications to given profile IDs"
  },
  "activationId": "ef73df5a-28e7-lb57-aff1-ha5ec14238af",
  "elapsedTime": 6
}
```