# Cortex fabric example repository

This repository contains examples for help Cortex fabric users.  
Each folder in the repository contains a standalone example consisting of:
 - Agents
 - Skills
 - Actions/Docker images
 - Sample Data
 - Scripts

## Contents
 | Folder | Language |Description |
 | --------| -------- |----------- |
 |  [APIExamples](./APIExamples) | JSON | Postman collection and environment files to be imported to your Postman app to help you get started with Cortex Fabric OpenAPIs |
 | [HelloWorldDaemon/](./HelloWorldDaemon) | Python | A simple agent with a single flask app |
 | [JobChaining/](./JobChaining) | Python | An agent with two jobs one generating a data file and the second job consuming a generated file.  The jobs use managed content for read/write file content.
 | [JobWebhook/](./JobWebhook) | Python | An agent with two jobs one generating messages and the second job POSTing the payload to a webhook. The jobs use skill properties.
