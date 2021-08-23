---
sidebar_position: 6
---

# Development

## How to build the plugin

`./gradlew clean build publishToMavenLocal snapshot`

This command will do a clean build and publish it as a snapshot version to your local maven repository.
So that in the project where you use the plugin you can just point to a snapshot to test your changes.  

Example:

```groovy
buildscript {
    repositories {
        maven {
            url "https://plugins.gradle.org/m2/"
        }
    }
    dependencies {
        classpath("com.xebialabs.gradle.plugins:integration-server-gradle-plugin:10.3.0-SNAPSHOT")
    }
}
```

## Where documentation resides

You can find the documentation to edit in documentation/docs folder. The `docs` folder contains built documentation 
which is served on GitHub Pages.

## How to run documentation site locally

`./gradlew yarnRunStart`

The site will be opened automatically in your default browser on page: [http://localhost:3000/integration-server-gradle-plugin/](http://localhost:3000/integration-server-gradle-plugin/) 

## How to generate the documentation for GitHub

`./gradlew docBuild` and commit all modified files in docs folder.

## Troubleshooting

In case you have to debug the plugin in the application, you can add a parameter `-Dorg.gradle.debug=true`.

The full command can look like this:

```shell script
./gradlew clean startIntegrationServer --stacktrace -Dorg.gradle.debug=true --no-daemon
```

Then in Intellij IDEA you are connecting to remote port 5005. The gradle task will proceed executing only after 
you will be connected to this port. If you have some troubles with it, you might need first to execute: `./gradlew --stop`

When you run the job on CI pipeline, and the error doesn't give a clue what is going on, it's better to add `--stactrace`
to get a better idea where exactly it fails. 

