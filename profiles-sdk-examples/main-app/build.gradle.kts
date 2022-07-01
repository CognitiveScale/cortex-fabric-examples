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
    implementation(platform("com.c12e.cortex.profiles:platform-dependencies:6.3.0-M.2"))
    implementation("com.c12e.cortex.profiles:profiles-sdk:6.3.0-M.2")
    implementation("com.google.cloud.spark:spark-bigquery-with-dependencies_2.12:0.25.0")

    // other examples
    implementation(project(":local-clients"))
    implementation(project(":join-connections"))
    implementation(project(":datasource-refresh"))

    // CLI framework
    implementation("info.picocli:picocli:4.6.3")
    annotationProcessor("info.picocli:picocli-codegen:4.6.3")

    // test dependencies
    testImplementation(platform("org.junit:junit-bom:5.8.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit-pioneer:junit-pioneer:1.7.1")
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
