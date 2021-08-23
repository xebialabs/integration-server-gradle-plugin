---
sidebar_position: 1
---

# Installation

## Requirements

Integration Server based on Gradle and docker images. Therefore, you have to have on your machine pre-installed:

* JDK 11
* Docker
* Docker Compose  

## Project structure

```
my-project
├── gradle
│   └── wrapper
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── build.gradle
├── gradlew
└── gradlew.bat
```

* `gradle-wrapper.properties` - points to the version of Gradle.
* `build.gradle` - contains all configurations, all your changes you have to do here.
* `gradlew` - executable file for Unix
* `gradlew.bat` - executable file for Windows

## Running the integration server

* `./gradlew startIntegrationServer` - starts the server
* `./gradlew clean startIntegrationServer` - cleans previously generated files/folders and then starts the server
* `./gradlew startIntegrationServer --stacktrace` - starts the server and in case of any issues will display a stacktrace for troubleshooting. 
* `./gradlew startIntegrationServer --stacktrace -Dorg.gradle.debug=true --no-daemon` - starts the server in debug mode.
In this mode you have to attach on remote debugging port 5005, it will wait before starting the server. It's useful if you have to debug 
the plugin. 
