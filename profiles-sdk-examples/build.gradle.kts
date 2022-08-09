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

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "1.6.21" apply false
	kotlin("plugin.serialization") version "1.6.21" apply false
	id("com.bmuschko.docker-remote-api") version "7.4.0" apply false
}

buildscript {
	repositories {
		mavenLocal()
		jcenter()
		mavenCentral()
	}
}

allprojects {
	group = "com.c12e.cortex.examples"
	version = "1.0.0-SNAPSHOT"

	tasks.withType<Test> {
		useJUnitPlatform()
	}

	tasks.withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }

	tasks.withType<KotlinCompile> {
		kotlinOptions {
			freeCompilerArgs = listOf("-Xjsr305=strict", "-opt-in=kotlin.RequiresOptIn")
			jvmTarget = "11"
			apiVersion = "1.6"
			languageVersion = "1.6"
		}
	}
}

val artifactoryUser: String by project
val artifactoryPassword: String by project

subprojects {
	repositories {
		mavenLocal()
		mavenCentral()
		jcenter()
		maven {
			url = uri("https://cognitivescale.jfrog.io/artifactory/cs-maven-local")
			credentials {
				username = artifactoryUser
				password = artifactoryPassword
			}
			authentication {
				create<BasicAuthentication>("basic")
			}
		}
		maven {
			url = uri("https://cognitivescale.jfrog.io/artifactory/cs-maven-local")
			credentials {
				username = artifactoryUser
				password = artifactoryPassword
			}
			authentication {
				create<BasicAuthentication>("basic")
			}
			metadataSources {
				artifact() //Look directly for artifact
			}
		}
		maven {
			url = uri("https://s3.amazonaws.com/redshift-maven-repository/release")
			name = "Redshift Maven Repository"
		}
	}
	configurations.all {
		resolutionStrategy {
			eachDependency {
				when (requested.group) {
					"com.c12e.cortex.profiles" -> useVersion("6.3.0-M.2.5")
				}
			}
		}
	}
}
