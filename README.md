To add plugin:

Inside of your **build.gradle** define plugin dependency like this:

```groovy
buildscript {
    repositories {
        jcenter()
        mavenCentral()
        mavenLocal()
        ["public", "snapshots", "releases", "alphas"].each { r ->
            maven {
                credentials {
                    username nexusUserName
                    password nexusPassword
                }
                url "${nexusBaseUrl}/repositories/${r}"
            }
        }
    }

    dependencies {
        classpath "com.xebialabs.gradle.plugins:integration-server-gradle-plugin:0.0.1-SNAPSHOT"
    }
}

apply plugin: 'integration.server'
```