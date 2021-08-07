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
        classpath "com.xebialabs.gradle.plugins:integration-server-gradle-plugin:10.3.0-807.1406"
    }
}

apply plugin: 'integration.server'
```

### Example integration server configuration

```groovy
integrationServer {
    servers {
        controlPlane {
            contextRoot = "/custom" // By default "/", but you can customize it
            debugPort = 4005 // Debug port, by default it is disabled
            httpPort = 4516 // Server HTTP port, by default it is random port
            jvmArgs = ["-Xmx1024m", "-Duser.timezone=UTC"] // custom Java process arguments
            logLevels = ["com.xebialabs.deployit.plugin.stitch": "debug"] // Log level overwrites
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
            runtimeDirectory = "server-runtime" // If to specify this directory, Deploy will be started from this folder and will not download it from external provider (Nexus)
            version = '10.2.0' // Version of the Server. By default it takes it from project property `xlDeployVersion`.
            yamlPatches = [ // Overwrites YAML file properties (create the file if it didn't exist yet)
                'centralConfiguration/deploy-server.yaml': [
                    'deploy.server.hostname': 'test.xebialabs.com',
                    'deploy.server.label': 'XLD'
                ]
            ]     
        }       
    }   
    
    workers {
        // By default we need only name, debugPort is disabled and port will be auto-generated from free ports
        // if directory is not specified then we run worker from the xl-deploy-server as local worker.
        // if directory is specified, then value should be absolute path
        worker01 { // name = worker01, worker01 will start from the same server directory as local worker(xl-deploy-10.2.0-server)
        }
        worker02 { // name = worker02, worker02 will start from the same server directory as local worker (xl-deploy-10.2.0-server)
            debugPort = 5006
            debugSuspend = true // by default false
            jvmArgs = ["-Xmx1024m", "-Duser.timezone=UTC"]
        }
        worker03 { // name = worker03, worker03 will start from the mentioned directory path(/opt/xl-deploy-worker)
            debugPort = 5007
            directory = "/opt/xl-deploy-worker"
            debugSuspend = false
            jvmArgs = ["-Xmx1024m", "-Duser.timezone=UTC"]
            port = 8182
        }
    }
}
```

#### Tasks

* `dockerComposeDatabaseStart` - starts containers required by the server
* `dockerComposeDatabaseStop` - stops containers required by the server
* `ImportDbUnitDataTask` - imports data files into a database
* `prepareDatabase` - copies configuration files for the selected database
* `startIntegrationServer` 
  - starts an integration server with a provided configuration and a database.
  - if the integrationServer needs to be started with the external worker ,we need to add the below configuration in build.gradle. if not integration server will start with in-process-worker.
   ```grovvy
   workers {      
        worker03 { // name = worker03, worker03 will start from the mentioned directory path(/opt/xl-deploy-worker)
            debugPort = 5007
            directory = "/opt/xl-deploy-worker"
            debugSuspend = false
            jvmArgs = ["-Xmx1024m", "-Duser.timezone=UTC"]
            port = 8182
        }
    }
```
* `shutdownIntegrationServer` - stops a database server and also stop a database
* `startSatellite` - starts satellite.
* `shutdownSatellite` - stops satellite.
#### Flags
* `-Pdatabase` - sets a database to launch, options: `derby-inmemory`, `derby-network`, `mssql`, `mysql`, `mysql-8`, `oracle-19c-se`, `postgres`
* `-PderbyPort` - provides Derby port if Derby database is used
* `-PlogSql` - enables printing of SQL queries executed by the server
* `-PserverDebugPort` - provides a server debug port for remote debugging
* `-PserverHttpPort` - provides an http port, overrides a configuration option

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

