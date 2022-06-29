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

## TODO
* additional unit tests
* scripting work and templating of profile jar version and created docker image
* how do you run with local instance of profiles-jar

## Local Developer Setup

To work with a locally built Profiles SDK JAR you can update the dependencies in `build.gradle.kts`, from:
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
    // can be replaced with Spark + Hadoop dependencies
    implementation(platform("com.c12e.cortex.profiles:parent:6.3.0-M.2"))
    
    // include local profiles-cli and java-graphql-client-builder JAR files and dependencies
    implementation(fileTree(mapOf(
        "dir" to  "<path to profiles-cli jar>/",
        "include" to listOf("<name-of-your>.jar")
    )))
    implementation(fileTree(mapOf(
        "dir" to "<path to java-graphql-client-builder jar >",
        "include" to listOf("<name-of-your>.jar")
    )))
    implementation("com.jayway.jsonpath:json-path:2.4.0")
    implementation("com.netflix.graphql.dgs.codegen:graphql-dgs-codegen-client-core:5.1.17")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
    implementation("org.apache.hadoop:hadoop-aws:3.3.2")
    implementation("com.charleskorn.kaml:kaml:0.35.3")
    implementation("com.google.inject:guice:5.1.0")
    implementation("info.picocli:picocli:4.6.3")
}
```

## Milestones

### Milestone 1
* Connector and DataSource work

### Milestone 2
* Streaming DataSource and Profile work
