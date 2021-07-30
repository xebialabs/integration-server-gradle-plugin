# Integration server plugin

Gradle plugin designed to provide XL Deploy integration server functionality.

## Usage

### In order to add the plugin

In the root file **build.gradle** of your project define a plugin dependency like this:

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
        classpath "com.xebialabs.gradle.plugins:integration-server-gradle-plugin:0.0.1-alpha.13"
    }
}

apply plugin: 'integration.server'
```

### Example integration server configuration

```groovy
integrationServer {
    serverHttpPort = 4516 // Default HTTP port 
    derbyPort = 55661 // Default derby port, if Derby database is used
    overlays = [
        plugins          : [
            "com.xebialabs.deployit.plugins:xld-ci-explorer:${xldCiExplorerVersion}@xldp", 
        ], // List of plugins to install 
        stitch           : ["${ciExplorerDataDependency}:stitch@zip"], // Creates a folder "stitch" with copied content of zip archive 
        conf             : [
            "${ciExplorerDataDependency}:configuration@zip",
            files("src/test/xld/deployit-license.lic")
        ], // Additional configuration files, e.g. license or archived configuration files
        lib              : [project.tasks.getByName("jar").outputs.files], // List of libraries to install in lib directory
        ext              : ["${ciExplorerDataDependency}:extensions@zip"], // List of extensions to install
        'derbydb/xldrepo': ["${ciExplorerDataDependency}:repository@zip"], // Derby data files, if Derby is used
        'build/artifacts': ["${ciExplorerDataDependency}:artifacts@zip"], // List of artifacts to import
    ]
    logLevels = ["com.xebialabs.deployit.plugin.stitch": "debug"] // Log level overwrites
    yamlPatches = [
        'centralConfiguration/deploy-server.yaml': [
            'deploy.server.hostname': 'test.xebialabs.com',
            'deploy.server.label': 'XLD'
        ]
    ] // overwrites yaml content
}
```

#### Tasks

* `dockerComposeDatabaseStart` - starts containers required by the server
* `dockerComposeDatabaseStop` - stops containers required by the server
* `ImportDbUnitDataTask` - imports data files into a database
* `integrationServer` - Starts an integration server
* `prepareDatabase` - copies configuration files for the selected database
* `startIntegrationServer` - starts an integration server with a provided configuration and a database
* `shutdownIntegrationServer` - stops a database server and also stop a database



#### Flags

* `-Papplication` - Starts the application
* `-Pdatabase` - sets a database to launch, options: `derby-inmemory`, `derby-network`, `mssql`, `mysql`, `mysql-8`, `oracle-19c-se`, `postgres`
* `-PderbyPort` - provides Derby port if Derby database is used
* `-PexternalWorker` - if enabled , it will start the XLDserver with external worker.
                     - `./gradlew :integration-test:integrationServer -Papplication=startXLDServer -PexternaleWorker=true`
                     - `./gradlew :integration-test:integrationServer -Papplication=shutdownXLDServer`
* `-PlogSql` - enables printing of SQL queries executed by the server
* `-PserverDebugPort` - provides a server debug port for remote debugging
* `-PserverHttpPort` - provides an http port, overrides a configuration option

#### application flags
* `-Papplication=startXLDServer` - starts the integration server
* `-Papplication=shutdownXLDServer` - shutdown the integration server, we need to mention the port as well(`./gradlew :integration-test:integrationServer -Papplication=shutdownXLDServer -PserverHttpPort=87989 `)
* `-Papplication=startXLDServer -PexternalWorker=true` -starts the integration server with external worker with default mq(eg: rabbitmq)
* `-Papplication=shutdownXLDServer -PexternalWorker=true` -shutdown the integration server with external worker
* `-Papplication=startXLDServer -PexternalWorker=true -Pmq=activemq` -starts the integration server with external worker with activemq
* `-Papplication=shutdownXLDServer -PexternalWorker=true` -shutdown the integration server with external worker
* `-Papplication=startSatelliteServer` - starts the satellite server
* `-Papplication=shutdownSatelliteServer` - shutdown the satellite server
## Limitations

* `mssql`, `mysql`, `mysql-8`, `oracle-19c-se`, `postgres` are started in a docker container
* `derby-inmemory`, `derby-network` are started as a Java process on a host machine
* Only  `mysql`, `mysql-8`, `postgres` can be started at the moment with the integration server
* `mssql`, `oracle-19c-se` require building an image at the moment and cannot be started by the integration server
* `postgres` is the only database which fully support data import
* `derby-inmemory`, `derby-network` do not support DbUnit data import, as these databases are not supported, use old data export format

## Development

Jenkins Job to run the build: https://jenkins-ng.xebialabs.com/jenkinsng/job/Gradle%20Plugins/job/integration-server-gradle-plugin/

Jenkins Job to release a new version: https://jenkins-ng.xebialabs.com/jenkinsng/job/Gradle%20Plugins/job/Release/job/Release%20integration-server-gradle-plugin/

