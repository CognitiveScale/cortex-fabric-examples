plugins {
    java
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
    api(platform("com.c12e.cortex.profiles:platform-dependencies"))
    api("com.c12e.cortex.profiles:profiles-sdk")
    implementation(project(":local-clients"))

    // CLI framework
    api("info.picocli:picocli:4.6.3")
    annotationProcessor("info.picocli:picocli-codegen:4.6.3")

    // test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.junit-pioneer:junit-pioneer:1.7.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

project.setProperty("mainClassName", "com.c12e.cortex.examples.cdata.CData")
