import com.github.gradle.node.yarn.task.YarnTask
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

buildscript {
    dependencies {
        classpath("com.xebialabs.gradle.plugins:gradle-commit:${properties["gradleCommitPluginVersion"]}")
    }
}

plugins {
    kotlin("jvm") version "1.8.10"
    `kotlin-dsl-base`

    id("com.github.node-gradle.node") version "4.0.0"
    id("idea")
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
    id("maven-publish")
    id("nebula.release") version "17.2.1"
    id("signing")
}

apply(plugin = "ai.digital.gradle-commit")

group = "com.xebialabs.gradle.plugins"
project.defaultTasks = listOf("build")

val releasedVersion = "25.1.0-${LocalDateTime.now().format(DateTimeFormatter.ofPattern("Mdd.Hmm"))}"
project.extra.set("releasedVersion", releasedVersion)

repositories {
    mavenLocal()
    gradlePluginPortal()
    maven {
        url = uri("https://plugins.gradle.org/m2/")
    }
    maven {
        url = uri("https://oss.sonatype.org/service/local/repositories/releases/content")
    }
    if (project.hasProperty("nexusBaseUrl")) {
        maven {
            credentials {
                username = project.property("nexusUserName") as String
                password =  project.property("nexusPassword") as String
            }
            url = uri("${project.property("nexusBaseUrl")}/repositories/releases")
        }
    }
}

idea {
    module {
        setDownloadJavadoc(true)
        setDownloadSources(true)
    }
}

dependencies {
    implementation(gradleApi())
    implementation(gradleKotlinDsl())

    implementation("com.fasterxml.jackson.core:jackson-databind:${properties["jacksonVersion"]}")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${properties["jacksonVersion"]}")
    implementation("com.typesafe:config:${properties["typesafeConfigVersion"]}")
    implementation("com.xebialabs.gradle.plugins:gradle-xl-derby-plugin:${properties["gradleXlDerbyPluginVersion"]}")
    implementation("commons-io:commons-io:${properties["commonsIOVersion"]}")
    implementation("com.palantir.gradle.docker:gradle-docker:${properties["dockerPluginVersion"]}")
    implementation("de.vandermeer:asciitable:${properties["asciitableVersion"]}")
    implementation("org.jsoup:jsoup:${properties["jsoupVersion"]}")
    implementation("com.mysql:mysql-connector-j:${properties["driverVersions.mysql"]}")
    implementation("net.jodah:failsafe:${properties["failsafeVersion"]}")
    implementation("org.codehaus.groovy.modules.http-builder:http-builder:${properties["httpBuilderVersion"]}") {
        exclude("org.codehaus.groovy", "groovy")
    }
    implementation("org.dbunit:dbunit:${properties["dbUnitVersion"]}")
    implementation("org.jetbrains.kotlin:kotlin-allopen:${properties["kotlin"]}")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${properties["kotlin"]}")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${properties["kotlin"]}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${properties["kotlin"]}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${properties["coroutinesVersion"]}")
    implementation("org.postgresql:postgresql:${properties["driverVersions.postgres"]}")


    testImplementation("io.mockk:mockk:1.13.5")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.0")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
    withJavadocJar()
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

if (project.hasProperty("sonatypeUsername") && project.hasProperty("public")) {
    publishing {
        publications {
            register("pluginMaven", MavenPublication::class) {
                from(components["java"])

                groupId = "com.xebialabs.gradle.plugins"
                artifactId = "integration-server-gradle-plugin"
                version = releasedVersion

                pom {
                    name.set("Integration Server Gradle Plugin")
                    description.set("The easy way to get custom setup for Deploy up and running")
                    url.set("https://github.com/xebialabs/integration-server-gradle-plugin.git")
                    licenses {
                        license {
                            name.set("GPLv2 with Digital.ai FLOSS License Exception")
                            url.set("https://github.com/xebialabs/integration-server-gradle-plugin/blob/master/LICENSE")
                        }
                    }

                    scm {
                        url.set("https://github.com/xebialabs/integration-server-gradle-plugin")
                    }

                    developers {
                        developer {
                            id.set("bnechyporenko")
                            name.set("Bogdan Nechyporenko")
                            email.set("bnechyporenko@digital.ai")
                        }
                        developer {
                            id.set("aalbul")
                            name.set("Alexander Albul")
                            email.set("aalbul@digital.ai")
                        }
                        developer {
                            id.set("sishwarya")
                            name.set("Ishwarya Surendrababu")
                            email.set("sishwarya@digital.ai")
                        }
                        developer {
                            id.set("tonac")
                            name.set("Antonio Sostar")
                            email.set("asostar@digital.ai")
                        }
                    }
                }
                versionMapping {
                    usage("java-api") {
                        fromResolutionOf("runtimeClasspath")
                    }
                    usage("java-runtime") {
                        fromResolutionResult()
                    }
                }
            }
        }
        repositories {
            maven {
                url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
                credentials {
                    username = project.property("sonatypeUsername").toString()
                    password = project.property("sonatypePassword").toString()
                }
            }
            maven {
                url = uri("https://oss.sonatype.org/content/repositories/snapshots")
                credentials {
                    username = project.property("sonatypeUsername").toString()
                    password = project.property("sonatypePassword").toString()
                }
            }
        }
    }

    signing {
        sign(publishing.publications["pluginMaven"])
    }

    nexusPublishing {
        repositories {
            sonatype {
                username.set(project.property("sonatypeUsername").toString())
                password.set(project.property("sonatypePassword").toString())
            }
        }
    }
} else {
    publishing {
        publications {
            register("pluginMaven", MavenPublication::class) {
                from(components["java"])
            }
        }
        repositories {
            maven {
                url = uri("${project.property("nexusBaseUrl")}/repositories/releases")
                credentials {
                    username = project.property("nexusUserName").toString()
                    password = project.property("nexusPassword").toString()
                }
            }
        }
    }
}

tasks {
    register("dumpVersion") {
        doLast {
            file(buildDir).mkdirs()
            file("$buildDir/version.dump").writeText("version=${releasedVersion}")
        }
    }

    named<YarnTask>("yarn_install") {
        args.set(listOf("--mutex", "network"))
        workingDir.set(file("${rootDir}/documentation"))
    }

    register<YarnTask>("yarnRunStart") {
        dependsOn(named("yarn_install"))
        args.set(listOf("run", "start"))
        workingDir.set(file("${rootDir}/documentation"))
    }

    register<YarnTask>("yarnRunBuild") {
        dependsOn(named("yarn_install"))
        args.set(listOf("run", "build"))
        workingDir.set(file("${rootDir}/documentation"))
    }

    register<Delete>("docCleanUp") {
        delete(file("${rootDir}/docs"))
        delete(file("${rootDir}/documentation/build"))
        delete(file("${rootDir}/documentation/.docusaurus"))
        delete(file("${rootDir}/documentation/node_modules"))
    }

    register<Copy>("docBuild") {
        dependsOn(named("yarnRunBuild"), named("docCleanUp"))
        from(file("${rootDir}/documentation/build"))
        into(file("${rootDir}/docs"))
    }

    register<GenerateDocumentation>("updateDocs") {
        dependsOn(named("docBuild"))
    }

    register<NebulaRelease>("nebulaRelease") {
        dependsOn(named("updateDocs"))
    }

    register("uploadArchives") {
        group = "upload"
        dependsOn(named("publish"))
    }

    compileKotlin {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
    }

    compileTestKotlin {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
    }

    withType<Test>().configureEach {
        jvmArgs(listOf("--add-opens=java.base/java.lang=ALL-UNNAMED",
            "--add-opens=java.base/java.util=ALL-UNNAMED"))
    }

    withType<ValidatePlugins>().configureEach {
        failOnWarning.set(false)
        enableStricterValidation.set(false)
    }
}
node {
    version.set("16.17.0")
    yarnVersion.set("1.22.19")
    download.set(true)
}
