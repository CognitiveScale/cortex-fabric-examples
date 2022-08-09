# Internal Developer Notes

## Prerequisites
* Set the [recommended JVM settings](../README.md#recommended-jvm-settings).
* Follow the [JFrog Artifactory setup](../README.md#jfrog-artifactory-setup).

## Local Developer Setup
To work with a locally built Profiles SDK JAR **in a container** put the built Profiles SDK jar file in
`main-app/src/main/resources/lib/` to override the version used in the Docker container by the Spark executors.

If you have built `distro` jar with the SDK and all dependencies, you can update `build.gradle.kts`, from:

```kotlin
dependencies {
    implementation(platform("com.c12e.cortex.profiles:parent:6.3.0-M.2.1"))
    implementation("com.c12e.cortex.profiles:profiles-cli:6.3.0-M.2.1")
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

## Check Manual Links (Should be automated)

Would be nice to automate testing of links as part of unit testing, but couldn't find packages in Java. I've manually used npm packages (both don't work perfectly out of the box):
- https://github.com/webhintio/markdown-link-validator
- https://github.com/tcort/markdown-link-check

### TroubleShooting

#### Secret does not exist

When a secret does not exist then check:
- Which Secret Client is being used.
- If using a `LocalSecretClient` subclass, then check said implementation (java file) includes the secret.
- If using the Remote client, then check the Cortex Console or Cortex CLI to see if the secret exists.

```
...
java.lang.RuntimeException: Secret super_secret does not exist in project testi-69257
at com.c12e.cortex.profiles.client.LocalSecretClient.getSecret(LocalSecretClient.java:33)
at com.c12e.cortex.phoenix.Connection.resolveSecret(Schema.kt:492)
at com.c12e.cortex.phoenix.Connection.resolveSecret(Schema.kt:487)
at com.c12e.cortex.phoenix.Connection.getParamMap(Schema.kt:479)
at com.c12e.cortex.profiles.module.connection.DefaultCortexConnectionReader.getConnectionOptions(DefaultCortexConnectionReader.java:142)
at com.c12e.cortex.profiles.module.connection.DefaultCortexConnectionReader.read(DefaultCortexConnectionReader.java:187)
at com.c12e.cortex.profiles.module.connection.DefaultCortexConnectionReader$CortexReadParameters.load(DefaultCortexConnectionReader.java:281)
at com.c12e.cortex.profiles.module.connection.DefaultCortexConnectionReader$CortexReadParameters.load(DefaultCortexConnectionReader.java:239)
at com.c12e.cortex.examples.joinconn.JoinConnections.joinConnections(JoinConnections.java:47)
at com.c12e.cortex.examples.joinconn.JoinConnections.run(JoinConnections.java:37)
at picocli.CommandLine.executeUserObject(CommandLine.java:1939)
at picocli.CommandLine.access$1300(CommandLine.java:145)
at picocli.CommandLine$RunLast.executeUserObjectOfLastSubcommandWithSameParent(CommandLine.java:2358)
at picocli.CommandLine$RunLast.handle(CommandLine.java:2352)
at picocli.CommandLine$RunLast.handle(CommandLine.java:2314)
at picocli.CommandLine$AbstractParseResultHandler.execute(CommandLine.java:2179)
at picocli.CommandLine$RunLast.execute(CommandLine.java:2316)
at picocli.CommandLine.execute(CommandLine.java:2078)
at com.c12e.cortex.examples.Application.main(Application.java:37)
at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(Unknown Source)
at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(Unknown Source)
at java.base/java.lang.reflect.Method.invoke(Unknown Source)
at org.apache.spark.deploy.JavaMainApplication.start(SparkApplication.scala:52)
at org.apache.spark.deploy.SparkSubmit.org$apache$spark$deploy$SparkSubmit$$runMain(SparkSubmit.scala:955)
at org.apache.spark.deploy.SparkSubmit.doRunMain$1(SparkSubmit.scala:180)
at org.apache.spark.deploy.SparkSubmit.submit(SparkSubmit.scala:203)
at org.apache.spark.deploy.SparkSubmit.doSubmit(SparkSubmit.scala:90)
at org.apache.spark.deploy.SparkSubmit$$anon$2.doSubmit(SparkSubmit.scala:1043)
at org.apache.spark.deploy.SparkSubmit$.main(SparkSubmit.scala:1052)
at org.apache.spark.deploy.SparkSubmit.main(SparkSubmit.scala)
19:33:23.395 [shutdown-hook-0] INFO  o.s.jetty.server.AbstractConnector - Stopped Spark@6917bb4{HTTP/1.1, (http/1.1)}{0.0.0.0:4040}
19:33:23.400 [shutdown-hook-0] INFO  org.apache.spark.ui.SparkUI - Stopped Spark web UI at http://205c969f1419:4040
Pod Name:
Container State:
Termination Reason:
Exit Code: 0
```

#### Skill stays in ACTIVE state

* If the entrypoint for the Docker container is not `scuttle`, then Skill activation (Task) stays `ACTIVE`.
  Additionally, the Spark Driver Pod will not exit because the Istio-proxy continues running (This is only viewable with
  Kubernetes access). This can happen if you build the Skill template using a Dockerfile/Docker image meant to be run
  locally. You must delete the corresponding Task (`cortex task delete`), and manually delete the Pod.

#### Guice Injection Errors

If you see a `NullPointerException`, `ClassNotFoundException`, or Guice DI related exception, then double check that ALL
configuration options are set correctly, and refer to the configuration options.

The issue is usually the result of misconfiguration or a class path issue (the class is not found).

EXAMPLE:
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

#### GraphQL Related Errors

* GraphQL 302 HTTP response -> double check API endpoint and Cortex Token
* GraphQL general non-200 HTTP response related to connections -> Verify that all Cortex resources exist and are set in the configuration options.

Connection By Name
```bash
curl -i -H 'Content-Type: application/json' \
 -H "Authorization: Bearer ${CORTEX_TOKEN}" \
 -X POST -d '{"query":"query {connectionByName(project: \"laguirre-testi-69257\", name: \"member-base\"){ allowRead allowWrite description name project   { name } title connectionType    contentType    params   { name value } } }","variables":{},"operationName":null}' https://api.dci-dev.dev-eks.insights.ai/fabric/v4/graphql
```

## Examples

Example flows table:

| Example                                                   | All Local (Spark, Catalog, Secrets, Backend Storage) | All Local (in container) | Semi-Local (Local Spark, Local Secrets, Local Backend, Remote Catalog) | Semi-Local (Local Spark, Local Secrets, Remote Catalog + Backend Storage) | Job Skill (In Cluster Spark Session + Cortex clients) |
|-----------------------------------------------------------|------------------------------------------------------|--------------------------|------------------------------------------------------------------------|---------------------------------------------------------------------------|-------------------------------------------------------|
| [join-connections](../join-connections/README.md)         | [x]                                                  | [x]                      | [x]                                                                    | [x]                                                                       | [x]                                                   |
| [datasource-refresh](../datasource-refresh/README.md)     | [x]                                                  | [x]                      | [x]                                                                    | [x]                                                                       | [x]                                                   |
| [build-profiles](../build-profiles/README.md)             | [x]                                                  | [x]                      | [ ]                                                                    | [ ]                                                                       | [x]                                                   |
| [datasource-streaming](../datasource-streaming/README.md) | [x]                                                  | [x]                      | [ ]                                                                    | [ ]                                                                       | [x]                                                   |
| [cdata-connection](../cdata-connection/README.md)         | [x]                                                  | [x]                      | [ ]                                                                    | [ ]                                                                       | [x]                                                   |
| [bigquery-connection](../bigquery-connection/README.md)   | [x]                                                  | [x]                      | [ ]                                                                    | [ ]                                                                       | [x]                                                   |

**NOTES:**
* It is also possible to run with a non-local Spark Session, but not including that as it requires Kubernetes access and not practical for a lot of users.

## TODOs
* additional unit tests
* scripting work and templating of profile jar version and created docker image
* how do you run with local instance of profiles-jar

## Milestones

### Milestone 1
* Connector and Data Source work

### Milestone 2
* Streaming Data Source and Profile work
