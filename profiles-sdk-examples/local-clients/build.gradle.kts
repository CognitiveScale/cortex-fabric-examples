
plugins {
    java
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

    // test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.junit-pioneer:junit-pioneer:1.7.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
