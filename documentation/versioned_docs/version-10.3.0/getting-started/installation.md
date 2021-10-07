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

## Add the plugin

In the root file **build.gradle** of your project define a plugin dependency like this:

```groovy

buildscript {
    repositories {
        mavenCentral()
        mavenLocal() // Optional, only required if you'll develop changes to the plugin.
    }

    dependencies {
        classpath "com.xebialabs.gradle.plugins:integration-server-gradle-plugin:10.3.0-820.1249"
    }
}

apply plugin: 'integration.server'

integrationServer {
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
