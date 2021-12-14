---
sidebar_position: 1
---

# Installation

## Requirements

Integration Server based on Gradle and docker images. Therefore, you have to have on your machine pre-installed:

* JDK 11
* Docker
* Docker Compose  
* Gradle 6+

If you want to provision a Kubernetes cluster on cloud with help of terraform first you have to ensure that you have installed it along with the
provider cli tool. Please keep in mind, terraform uses cli tool configurations if not specified explicitly as
in this project ( e.g. aws region value will be read from .aws/config )

## Add the plugin

In the root file **build.gradle** of your project define a plugin dependency like this:

```groovy

buildscript {
    repositories {
        mavenCentral()
        maven { // Public repository of Digital.ai, for example from here served CLI
            url 'https://nexus.xebialabs.com/nexus/content/repositories/digitalai-public'
        }
        mavenLocal() // Optional, only required if you'll develop changes to the plugin.
    }

    dependencies {
        classpath "com.xebialabs.gradle.plugins:integration-server-gradle-plugin:10.3.0-820.1249"
    }
}

apply plugin: 'integration.server'

deployIntegrationServer {
    servers {
        controlPlane {
            dockerImage = "xebialabs/xl-deploy" // docker hub repository
            version = "10.2.2" // Here you can point to a version you'd like to run
        }
    }
}
```

:::tip

This plugin version works only with Deploy 10.2.x and 10.3.x, you have to match the plugin version with Deploy version. <br/> 
It might work with one minor version up or down, but there is no guarantee.  

:::

## Running the integration server

* `./gradlew startIntegrationServer` - starts the server
* `./gradlew clean startIntegrationServer` - cleans previously generated files/folders and then starts the server
* `./gradlew startIntegrationServer --stacktrace` - starts the server and in case of any issues will display a stacktrace for troubleshooting. 
* `./gradlew startIntegrationServer --stacktrace -Dorg.gradle.debug=true --no-daemon` - starts the server in debug mode.
In this mode you have to attach on remote debugging port 5005, it will wait before starting the server. It's useful if you have to debug 
the plugin. 
