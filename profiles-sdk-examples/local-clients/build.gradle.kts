
plugins {
    //java
    application
    kotlin("jvm")
    kotlin("plugin.serialization")
}

group = "com.c12e.cortex.examples"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // project dependencies
    api(platform("com.c12e.cortex.profiles:platform-dependencies:6.3.0-M.2"))
    api("com.c12e.cortex.profiles:profiles-sdk:6.3.0-M.2")

    // CLI framework
    api("info.picocli:picocli:4.6.3")
    annotationProcessor("info.picocli:picocli-codegen:4.6.3")

    // test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.junit-pioneer:junit-pioneer:1.7.1")
}

project.setProperty("mainClassName", "com.c12e.cortex.examples.local.Main")
distributions {
    main {
        contents {
            from("src/main/resources") {
                into("src/main/resources")
            }
        }
    }
}

application {
    mainClass.set("com.c12e.cortex.examples.local.Main")
    applicationName = "local-clients"
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}