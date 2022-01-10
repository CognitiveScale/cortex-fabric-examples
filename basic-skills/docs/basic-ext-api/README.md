# basic-ext-api External API Skill (UPDATE THIS FOR YOUR SKILL)

A system generated Skill based on the custom external API Skill template. This skill type allows users to wrap an external REST API as a Cortex Fabric Skill.

> Modify these sections for your specific use case.


## Files Generated
- `docs/` - The directory that houses the Skills' READMEs
  - `basic-ext-api/`: The directory that houses the basic-ext-api Skill's README
    - `README.md`: Provide the objectives, requirements, and instructions for generating and deploying the Skill here.
- `skills/`: The directory that houses the Skills
  - `basic-ext-api/`: The directory that houses the basic-ext-api Skill's assets
    - `invoke/`: Contains the payloads, organized by Skill input name, used to invoke the basic-ext-api Skill
      - `request/`: Contains payload files used to invoke the Skill
        - `message.json`: Write a test JSON payload to invoke the Skill
      - `skill.yaml`: Define basic-ext-api Skill definition and map Actions here


## Generate the Skill

The Skill should have been generated previously. If not, please use the following links for more information on how to continue building, pushing, deploying, developing, and invoking your Skill.
- [VS Code Extension](https://cognitivescale.github.io/cortex-code/)
- [Skill Builder in the Fabric Console](https://cognitivescale.github.io/cortex-fabric/docs/build-skills/skill-builder-ui)


> NOTE: Modify the following files for your specific use case:
> - `docs/basic-ext-api/README.md`
> - `skills/basic-ext-api/actions/skill.yaml`
>   - Required Properties: `url`, `path`, `method`, and `headers.content-type` per the targeted external API
>   - Optional Properties: `headers.authorization`
> - `invoke/request/message.json`


## Documentation
- [Cortex Fabric Documentation - Development - Develop Skills](https://cognitivescale.github.io/cortex-fabric/docs/development/define-skills)
- [Skill Elements](https://cognitivescale.github.io/cortex-fabric/docs/build-skills/define-skills#skill-elements)
- 
