# Job Skill Example

This workspace template demonstrates a simple job skill skeleton that simply prints out the job payload when invoked.

## Building and Publishing Skills

### Using the cortex cli

#### Building docker images

To build all docker images in the workspace:

```BASH
cortex workspaces build
```

To build all docker images in the folder:

```BASH
cortex workspaces build [folder]
```

To build only the docker image for the specified skill:

```BASH
cortex workspaces build --skill <skill name>
```

#### Deploying skills and docker images to a Cortex cluster

To publish all docker images and skills in the workspace:

```BASH
cortex workspaces publish
```

To publish all docker images and skills in the folder:

```BASH
cortex workspaces publish [folder]
```

To publish only the specified skill and its docker image:

```BASH
cortex workspaces publish --skill <skill name>
```

### Using the VS code extension (CognitiveScale.cortex-code)

In order to build the skill Docker image, simply select "Build Skills" from the Cortex menu in the IDE.  You can also right-click on the `skill.yaml` file for the skill and select "Build Skill" from the Cortex context menu.

Once the skill is built, you can publish it the same way, by selecting "Publish".
