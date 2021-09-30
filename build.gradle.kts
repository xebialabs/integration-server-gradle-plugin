import com.github.gradle.node.yarn.task.YarnTask
import com.xebialabs.gradle.release.NebulaRelease
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    id("com.github.node-gradle.node") version "3.1.0"
    id("groovy")
    id("idea")
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    id("maven-publish")
    id("nebula.release") version "15.3.1"
    id("org.jetbrains.kotlin.jvm") version "1.4.20"
    id("signing")
}

group = "com.xebialabs.gradle.plugins"
project.defaultTasks = listOf("build")

val releasedVersion = "10.4.0-${LocalDateTime.now().format(DateTimeFormatter.ofPattern("Mdd.Hmm"))}"
project.extensions.add("releasedVersion", releasedVersion)

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    mavenLocal()
    gradlePluginPortal()
    maven {
        url = uri("https://plugins.gradle.org/m2/")
    }
    maven {
        url = uri("https://oss.sonatype.org/service/local/repositories/releases/content")
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
    implementation(localGroovy())

    implementation("com.fasterxml.jackson.core:jackson-databind:${properties["jacksonVersion"]}")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${properties["jacksonVersion"]}")
    implementation("com.typesafe:config:${properties["typesafeConfigVersion"]}")
    implementation("com.xebialabs.gradle.plugins:gradle-xl-derby-plugin:${properties["gradleXlDerbyPluginVersion"]}")
    implementation("commons-io:commons-io:${properties["commonsIOVersion"]}")
    implementation("com.palantir.gradle.docker:gradle-docker:${properties["dockerPluginVersion"]}")
    implementation("de.vandermeer:asciitable:${properties["asciitableVersion"]}")
    implementation("mysql:mysql-connector-java:${properties["driverVersions.mysql"]}")
    implementation("org.codehaus.groovy.modules.http-builder:http-builder:${properties["httpBuilderVersion"]}") {
        exclude("org.codehaus.groovy", "groovy")
    }
    implementation("org.dbunit:dbunit:${properties["dbUnitVersion"]}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${properties["kotlin"]}")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${properties["kotlin"]}")
    implementation("org.jetbrains.kotlin:kotlin-allopen:${properties["kotlin"]}")
    implementation("org.postgresql:postgresql:${properties["driverVersions.postgres"]}")


    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.2")
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

if (project.hasProperty("sonatypeUsername") && project.hasProperty("public")) {
    publishing {
        publications {
            register("mavenJava", MavenPublication::class) {
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
} else {
    publishing {
        publications {
            create<MavenPublication>("myLibrary") {
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


if (project.hasProperty("sonatypeUsername") && project.hasProperty("public")) {
    signing {
        sign(publishing.publications["mavenJava"])
    }

    nexusPublishing {
        repositories {
            sonatype {
                username.set(project.property("sonatypeUsername").toString())
                password.set(project.property("sonatypePassword").toString())
            }
        }
    }
}

tasks {
    withType(NebulaRelease::class.java) {
        named("nebulaRelease")
    }

    register("dumpVersion") {
        file(buildDir).mkdirs()
        file("$buildDir/version.dump").writeText("version=$project.version")
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
        dependsOn(named("yarn_install"), named("docCleanUp"))
        from(file("${rootDir}/documentation/build"))
    }

    named<AbstractCompile>("compileGroovy") {
        dependsOn(named("compileKotlin"))
        // Groovy only needs the declared dependencies
        // (and not longer the output of compileJava)
        classpath = sourceSets.main.get().compileClasspath + files(compileKotlin.get().destinationDir)
    }

}

node {
    version.set("14.17.5")
    yarnVersion.set("1.22.11")
    download.set(true)
}
