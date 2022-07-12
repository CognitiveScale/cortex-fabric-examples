# Local Developer Setup

## Recommended JVM Settings

These can also be set/controlled through the `~/.gradle/gradle.properties` file that needs to be setup.
```
export GRADLE_OPTS="-Dorg.gradle.jvmargs='-Xmx2g -XX:MaxMetaspaceSize=512m -XX:+UseG1GC -XX:+UseStringDeduplication -XX:+OptimizeStringConcat'"
```

## JFrog Artifactory Setup

1. Java 11 (use Openjdk, see the link in [Resources](#resources) section)
1. JFrog Artifactory credentials (shared in LastPass with everyone in `Shared-Engineering` folder)
1. Install IntelliJ IDEA with the latest Kotlin plugin enabled ([Intellij IDEA](https://www.jetbrains.com/idea/))
1. Put JFrog Artifactory credentials in `$USER_HOME/.gradle/gradle.properties` file. See `gradle.properties.template` for instructions. 


## Local Developer Setup


To work with a locally built Profiles SDK JAR **in a container** you put built profiles-sdk jar in
`main-app/src/main/resources/lib/` to override the version used in the docker container (by the spark executors).

If you have built `distro` jar w/ the SDK and all dependencies, you can update `build.gradle.kts`, from:

```kotlin
dependencies {
    implementation(platform("com.c12e.cortex.profiles:parent:6.3.0-M.2"))
    implementation("com.c12e.cortex.profiles:profiles-cli:6.3.0-M.2")
    ...
}
```

To:
```kotlin
dependencies {
    // include local profiles-sdk, spark + hadoop, etc
    implementation(fileTree(mapOf(
        "dir" to  "<path to jar>/",
        "include" to listOf("<name-of-your>.jar")
    )))
}
```


### TroubleShooting

#### Guice Injection Errors

Usually the result of misconfiguration, ex:
```
com.google.inject.ProvisionException: Unable to provision, see the following errors:

1) [Guice/NullInjectedIntoNonNullable]: null returned by binding at CortexSession$CortexSessionModule.secretsClientUrl()
   but the 1st parameter of InternalSecretsClient.<init>(InternalSecretsClient.kt:54) is not @Nullable
   at CortexSession$CortexSessionModule.secretsClientUrl(CortexSession.java:224)
   at CortexSession$CortexSessionModule.secretsClientUrl(CortexSession.java:224)
   at InternalSecretsClient.<init>(InternalSecretsClient.kt:54)
   \_ for 1st parameter
   at InternalSecretsClient.class(InternalSecretsClient.kt:54)
   while locating InternalSecretsClient
   at DefaultCortexConnectionReader.<init>(DefaultCortexConnectionReader.java:56)
   \_ for 5th parameter
   while locating DefaultCortexConnectionReader
   at DefaultCortexSparkReader.<init>(DefaultCortexSparkReader.java:40)
   \_ for 1st parameter
   while locating DefaultCortexSparkReader
   at BaseCortexContext.<init>(BaseCortexContext.java:58)
   \_ for 3rd parameter
   while locating BaseCortexContext
   at CortexSession$CortexSessionModule.provideSession(CortexSession.java:205)
   \_ for 1st parameter
   at CortexSession$CortexSessionModule.provideSession(CortexSession.java:205)
   while locating CortexSession

Learn more:
https://github.com/google/guice/wiki/NULL_INJECTED_INTO_NON_NULLABLE

1 error

======================
Full classname legend:
======================
BaseCortexContext:                 "com.c12e.cortex.profiles.context.BaseCortexContext"
CortexSession:                     "com.c12e.cortex.profiles.CortexSession"
CortexSession$CortexSessionModule: "com.c12e.cortex.profiles.CortexSession$CortexSessionModule"
DefaultCortexConnectionReader:     "com.c12e.cortex.profiles.module.connection.DefaultCortexConnectionReader"
DefaultCortexSparkReader:          "com.c12e.cortex.profiles.reader.DefaultCortexSparkReader"
InternalSecretsClient:             "com.c12e.cortex.phoenix.InternalSecretsClient"
========================
End of classname legend:
========================
```

#### Inspecting the Built Container

```
docker run -p 4040:4040 \
    --entrypoint="/bin/bash"  -it
    -e "CORTEX_TOKEN=${CORTEX_TOKEN}" 
    -e "CDATA_OEM_KEY=${CDATA_OEM_KEY}"
    -e "CDATA_PRODUCT_CHECKSUM=${CDATA_PRODUCT_CHECKSUM}"
    -v $(pwd)/cdata-connection/src/main/resources/conf:/app/conf
    -v $(pwd)/main-app/src:/opt/spark/work-dir/src
    -v $(pwd)/main-app/build:/opt/spark/work-dir/build
    profiles-example
```

#### GQL Query Template:

Connection By Name
```bash
curl -i -H 'Content-Type: application/json' \
 -H "Authorization: Bearer ${CORTEX_TOKEN}" \
 -X POST -d '{"query":"query {connectionByName(project: \"laguirre-testi-69257\", name: \"member-base\"){ allowRead allowWrite description name project   { name } title connectionType    contentType    params   { name value } } }","variables":{},"operationName":null}' https://api.dci-dev.dev-eks.insights.ai/fabric/v4/graphql
```

## Examples

Table of which example flows have been fully documented:

| Example                                         | All Local (Spark, Catalog, Secrets, Backend Storage) | All Local (in container) | Semi-Local (Local Spark, Local Secrets, Local Backend, Remote Catalog) | Semi-Local (Local Spark, Local Secrets, Remote Catalog + Backend Storage) | Job Skill (In Cluster Spark Session + Cortex clients) |
|-------------------------------------------------|------------------------------------------------------|--------------------------|------------------------------------------------------------------------|---------------------------------------------------------------------------|-------------------------------------------------------|
| [join-connections](../join-connections)         | [x]                                                  | [x]                      | [x]                                                                    | [x]                                                                       | [x]                                                   |
| [datasource-refresh](../datasource-refresh)     | [x]                                                  | [x]                      | [x]                                                                    | [x]                                                                       | [x]                                                   |
| [build-profiles](../build-profiles)             | [x]                                                  | [x]                      | [ ]                                                                    | [ ]                                                                       | [ ]                                                   |
| [datasource-streaming](../datasource-streaming) | [x]                                                  | [x]                      | [ ]                                                                    | [ ]                                                                       | [ ]                                                   |
| [cdata-connection](../cdata-connection)         | [ ]                                                  | [x]                      | [ ]                                                                    | [ ]                                                                       | [ ]                                                   |
| [bigquery-connection](../bigquery-connection)   | [x]                                                  | [x]                      | [ ]                                                                    | [ ]                                                                       | [ ]                                                   |

Notes:
* It is also possible to run with a non-local Spark Session, but not including that as it requires kube access and not practical for a lot of users.

## TODOs
* additional unit tests
* scripting work and templating of profile jar version and created docker image
* how do you run with local instance of profiles-jar

## Milestones

### Milestone 1
* Connector and DataSource work

### Milestone 2
* Streaming DataSource and Profile work
