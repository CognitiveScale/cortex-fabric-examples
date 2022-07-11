/*
 * Copyright 2022 Cognitive Scale, Inc. All Rights Reserved.
 *
 *  See LICENSE.txt for details.
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    application
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.bmuschko.docker-remote-api")
}

dependencies {
    // project dependencies
    api(platform("com.c12e.cortex.profiles:platform-dependencies:6.3.0-M.2"))
    api("com.c12e.cortex.profiles:profiles-sdk:6.3.0-M.2")
    implementation("com.google.cloud.spark:spark-bigquery-with-dependencies_2.12:0.25.0")

    // any extra jars (for CData/BigQuery examples)
    runtimeOnly(fileTree("src/main/resources/lib"){ include("*.jar") })
    testRuntimeOnly(fileTree("src/main/resources/lib"){ include("*.jar") })

    // other examples
    implementation(project(":local-clients"))
    implementation(project(":join-connections"))
    implementation(project(":datasource-refresh"))
    implementation(project(":build-profiles"))
    implementation(project(":cdata-connection"))
    implementation(project(":streaming-datasource"))

    // CLI framework
    implementation("info.picocli:picocli:4.6.3")
    annotationProcessor("info.picocli:picocli-codegen:4.6.3")

    // test dependencies
    testImplementation(platform("org.junit:junit-bom:5.8.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit-pioneer:junit-pioneer:1.7.1")

    // other examples
    testImplementation(project(":local-clients"))
    testImplementation(project(":join-connections"))
    testImplementation(project(":datasource-refresh"))
    testImplementation(project(":build-profiles"))
    testImplementation(project(":cdata-connection"))
    testImplementation(project(":streaming-datasource"))
}

// application entrypoint
application {
    mainClass.set("com.c12e.cortex.examples.Application")
    applicationName = "cortex-profiles"
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = TestExceptionFormat.FULL
    }
}

project.setProperty("mainClassName", "com.c12e.cortex.examples.Application")
distributions {
    main {
        contents {
            from("src/main/resources") {
                into("src/main/resources")
            }
        }
    }
}
/*
tasks.create("docker-install", DockerBuildImage::class) {
    inputDir.set(file("docker"))
    images.add("test/myapp:latest")
}*/

tasks.withType<Jar> {
    setProperty("zip64", true)
    manifest.attributes["Main-Class"] = "com.c12e.cortex.examples.Application"
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    // include all compiled examples in built jar
    from(project.sourceSets["main"].output)
    from(project(":local-clients").sourceSets["main"].output)
    from(project(":build-profiles").sourceSets["main"].output)
    from(project(":datasource-refresh").sourceSets["main"].output)
    from(project(":join-connections").sourceSets["main"].output)
    from(project(":cdata-connection").sourceSets["main"].output)
    //from(project(":streaming-datasource").sourceSets["main"].output)
}
