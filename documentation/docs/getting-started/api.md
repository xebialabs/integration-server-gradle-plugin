---
sidebar_position: 4
---

# API

## Configuration

### The first section level

```groovy title=build.gradle
integrationServer {
    servers {}
    databases {}
    workers {}
    satellites {}
    mqDriverVersions {}
    xldIsDataVersion {}
}
```

|Name|Description|
| :---: | :---: |
|servers|Server configurations, currently, it's possible to configure only one.|
|databases|Currently supported only 1 running database. For now you can find this section helpful for overriding database driving versions or having more database level logs.|
|workers|You can configure as many workers as you need here.|
|satellites|You can configure as many satellites as you need here.|
|mqDriverVersions|Points to the version of MQ to use, in case you wish to adapt it to your own version.|
|xldIsDataVersion|**Only for internal use in Digital.ai** Points to the data which is going to be imported after server is booted. To run waste the time to generate a huge amount of test data.|

### Servers section

```groovy title=build.gradle
integrationServer {
   servers {
       controlPlane {
           cliDebugPort = 4004
           cliDebugSuspend = true
           contextRoot = "/custom"
           debugPort = 4005
           debugSuspend = true
           defaultOfficialPluginsToExclude = ["xld-terraform-plugin-10.1.0", "xld-aws-plugin-10.2.1"]
           devOpsAsCodes {
                first {
                    devOpAsCodeScript = file("${buildDir}/resources/main/xld/devopsAsCode/app-with-git-info.yaml")
                    scmAuthor = "John Doe <john.doe@organization.co>"
                    scmCommit = "6f13f85ca0fa3d7299f195a4a4b1bc95946b98a5"
                    scmDate = "2021-05-16T12:27:19.000Z"
                    scmFile = "integration-test/src/test/resources/yaml/xld/create/infrastructure.yaml"
                    scmMessage = "Create Infrastructure"
                    scmRemote = "git@github.com:xebialabs/integration-server-gradle-plugin.git"
                    scmType = "git"
                }
           }
           dockerImage = "xebialabs/xl-deploy" 
           httpPort = 4516
           generateDatasets = []
           jvmArgs = ["-Xmx1024m", "-Duser.timezone=UTC"]
           logLevels = ["com.xebialabs.deployit.plugin.stitch": "debug"]
           overlays = [
               plugins          : [
                   "com.xebialabs.deployit.plugins:xld-ci-explorer:${xldCiExplorerVersion}@xldp", 
               ], 
               conf             : [
                   "${ciExplorerDataDependency}:configuration@zip",
                   files("src/test/xld/deployit-license.lic")
               ],
               lib              : [project.tasks.getByName("jar").outputs.files],
               ext              : ["${ciExplorerDataDependency}:extensions@zip"],
               'build/artifacts': ["${ciExplorerDataDependency}:artifacts@zip"],
               'xldrepo': ["${ciExplorerDataDependency}:repository@zip"],
           ]
           pingRetrySleepTime = 5
           pingTotalTries = 120
           provisionSocketTimeout = 120000
           provisionScripts = []
           removeStdoutConfig = true
           runtimeDirectory = "server-runtime"
           version = '10.2.2'
           yamlPatches = [
               'centralConfiguration/deploy-server.yaml': [
                   'deploy.server.hostname': 'test.xebialabs.com',
                   'deploy.server.label': 'XLD'
               ]
           ]     
       }       
   }   
}
```

|Name|Type|Default Value|Description|
| :---: | :---: | :---: | :---: |
|cliDebugPort|Optional|None|Remote Debug Port for Deploy CLI| 
|cliDebugSuspend|Optional|false|Suspend the start of the process before the remoting tool is attached.| 
|contextRoot|Optional|/|The context root for Deploy. **Limitation:** *Doesn't work for docker setup*|
|debugPort|Optional|None|Remote Debug Port for Deploy Server| 
|debugSuspend|Optional|false|Suspend the start of the process before the remoting tool is attached.| 
|defaultOfficialPluginsToExclude|Optional|[]|The list of plugins which are going to be excluded from the **plugins/xld-official** before the booting the server. Expected input is the list of strings separated by comma. Exclusion is happening by name convention. If for example you will say "plugin", all plugins going to be removed.| 
|devOpsAsCodes|Optional|None|Read the explanation below, as this is a section.|
|dockerImage|Optional|None|When this property is specified, docker based setup will be performed. The name of the docker image, without version. Version is specified in the separate field or dedicated from gradle properties.|
|httpPort|Optional|Random port|The HTTP port for Deploy server.|
|generateDatasets|Optional|[]|The HTTP port for Deploy server. The url `"http://localhost:${server.httpPort}/deployit/generate/${dataset}"` is going to be hit. This URL point is not available in Deploy by default. How you can develop it, is going to be described soon in a blog.|
|jvmArgs|Optional|[]|JVM arguments which are going to be used on Server startup|
|logLevels|Optional|[:]|Custom log levels to be included in logback.xml configuration. Expected format is a map, where the key is the package name and value the log level.|
|overlays|Optional|[:]|Read the explanation below, as this is a section.|
|pingRetrySleepTime|Optional|10|During the startup of the server we check when it's completely booted. This property configures how long to sleep (in seconds) between retries.|
|pingTotalTries|Optional|60|During the startup of the server we check when it's completely booted. This property configures how many times to retry.|
|provisionScripts|Optional|[]|Provision scripts to be executed by task `runProvisionScript`. This subject is up for a change.|
|removeStdoutConfig|Optional|false|Modifies default logback.xml by removing STDOUT output.| 
|runtimeDirectory|Optional|None|When this property is specified, runtime directory setup will be performed. Just make sure that you have complete deploy instance present there.|
|version|Optional|None|It can be specified in several ways. Or as a gradle property `xlDeployVersion`, via parameter or in `gradle.properties` file or explicitly via this field.|
|yamlPatches|Optional|[:]|Read the explanation below, as this is a section.|

### Database section

```groovy title=build.gradle
integrationServer {
   databases {
     database01 {
        derbyPort = 10000
        driverVersions = [
             'mssql'        : '8.4.1.jre8',
             'mysql'        : '8.0.22',
             'mysql-8'      : '8.0.22',
             'oracle-19c-se': '21.1.0.0',
             'postgres-10'  : '42.2.9',
             'postgres-12'  : '42.2.23',
        ]
        logSql = true
     }   
   }
}
```

|Name|Type|Default Value|Description|
| :---: | :---: | :---: | :---: |
|derbyPort|Optional|Random number|If derby database is used, the port on which it's going to be started. For other databases port is fixed.|
|driverVersions|Optional|['mssql':'8.4.1.jre8','mysql':'8.0.22','mysql-8':'8.0.22','oracle-19c-se':'21.1.0.0','postgres-10':'42.2.9','postgres-12':'42.2.23']||
|logSql|Optional|false||

The most important what you have to know regarding the database configuration is, choosing which database to run is happening 
now on the level of project property `database`. It means that you can specify it in 2 ways:

* adding a parameter via `-P=database`
* In the root of your project in `gradle.properties` file

```properties title=gradle.properties
database=derby-network
```

If nothing specified derby in memory will be used.

## Tasks

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

## Flags

* `-Pdatabase` - sets a database to launch, options: `derby-inmemory`, `derby-network`, `mssql`, `mysql`, `mysql-8`, `oracle-19c-se`, `postgres`
* `-PderbyPort` - provides Derby port if Derby database is used
* `-PlogSql` - enables printing of SQL queries executed by the server
* `-PserverDebugPort` - provides a server debug port for remote debugging
* `-PsatelliteDebugPort` - provides a satellite debug port for remote debugging
* `-PserverHttpPort` - provides an http port, overrides a configuration option


