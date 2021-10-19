---
sidebar_position: 5
---

# Configuration

## The first section level

```groovy title=build.gradle
deployIntegrationServer {
    clis {}
    servers {}
    database {}
    workers {}
    satellites {}
    mqDriverVersions {}
    xldIsDataVersion {}
    tests {}
}
```

|Name|Description|
| :---: | :---: |
|clis|CLIs configurations, currently, it's possible to configure only one.|
|database|Database configuration, you can find this section helpful for overriding database driving versions or having more database level logs.|
|mqDriverVersions|Points to the version of MQ to use, in case you wish to adapt it to your own version.|
|satellites|You can configure as many satellites as you need here.|
|servers|Server configurations, currently, it's possible to configure only one.|
|tests|You can define Jython based test setups|
|xldIsDataVersion|**Only for internal use in Digital.ai** Points to the data which is going to be imported after server is booted. To run waste the time to generate a huge amount of test data.|
|workers|You can configure as many workers as you need here.|

## CLIs section

```groovy title=build.gradle
deployIntegrationServer {
    clis {
        cli { // The name of the section, you can name it as you with
            cleanDefaultExtContent = true
            copyBuildArtifacts = [
               lib: /(.+)[.](jar)/
            ]
            debugPort = 4005
            debugSuspend = true
            filesToExecute = [file("src/main/resources/provision.py")]
            overlays = [
                ext: [
                        files("ext") 
                ],
                lib: [
                        "com.xebialabs.xl-platform.test-utils:py-modules:${testUtilsVersion}@jar"
                ]
            ]
            socketTimeout = 120000
            version = "10.2.2"
        }
    }
}
```

|Name|Type|Default Value|Description|
| :---: | :---: | :---: | :---: |
|cleanDefaultExtContent|Optional|false|By default in CLI there are 3 files included in the ext with some helper functions. You can remove it if it clashes with your scripts.|
|copyBuildArtifacts|Optional|[:]|Here you can define what would you like to include to integration server from the build process itself. For example you run: `./gradlew build integrationServer` and you create `*.jar` of your plugin which you would like to include to integration server. You have to specify it here. As for overlay it won't work. With overlay to make it work you have to run 2 commands: `./gradlew build` and then `./gradlew startIntegrationServer`. Key is a relative folder name from CLI base, and a value is a pattern to all files located in `build` folder except `integration-server` sub-folder. This one is excluded.|
|debugPort|Optional|None|Remote Debug Port for Deploy CLI | 
|debugSuspend|Optional|false|Suspend the start of the process before the remoting tool is attached.|
|filesToExecute|Optional|[]|The list of files which will be executed after Deploy Server (workers and satellite if configured) started. You can use it to provision your server with data before running the tests.|
|overlays|Optional|[:]|[Read about this section below](#overlays)|
|socketTimeout|Optional|60000|Time is set in ms. Socket timeout means how long the socket will be opened to execute the provided script. If your script takes a time to be executed, consider to increase it.|
|version|Optional|None|It can be specified in several ways. Or as a gradle property `deployCliVersion`, via parameter or in `gradle.properties` file or explicitly via this field. As a last resource it also checks on `xlDeployVersion`, as usually the version should be the same, but you have a possibility to define different versions. |

## Servers section

```groovy title=build.gradle
deployIntegrationServer {
   servers {
       controlPlane { // The name of the section, you can name it as you with
           akkaSecured = true
           contextRoot = "/custom"
           copyBuildArtifacts = [
                "plugins/xld-official": /(.+)[.](xldp)/
           ]
           debugPort = 4005
           debugSuspend = true
           defaultOfficialPluginsToExclude = ["xld-terraform-plugin-10.1.0", "xld-aws-plugin-10.2.1"]
           devOpsAsCodes {
                first {
                    devOpAsCodeScript = file("${buildDir}/resources/main/xld/devopsAsCode/infrastructure.yaml")
                    scmAuthor = "John Doe <john.doe@organization.co>"
                    scmCommit = "6f13f85ca0fa3d7299f195a4a4b1bc95946b98a5"
                    scmDate = "2021-05-16T12:27:19.000Z"
                    scmFile = file("${buildDir}/resources/main/xld/devopsAsCode/infrastructure.yaml")
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
               'build/artifacts': ["${ciExplorerDataDependency}:artifacts@zip"],
               conf             : [
                   "${ciExplorerDataDependency}:configuration@zip",
                   files("src/test/xld/deployit-license.lic")
               ],
               ext              : ["${ciExplorerDataDependency}:extensions@zip"],
               lib              : [project.tasks.getByName("jar").outputs.files],
               'plugins/xld-official': [
                   "com.xebialabs.deployit.plugins:xld-ci-explorer:${xldCiExplorerVersion}@xldp", 
               ], 
               'xldrepo': ["${ciExplorerDataDependency}:repository@zip"],
           ]
           pingRetrySleepTime = 5
           pingTotalTries = 120
           removeStdoutConfig = true
           runtimeDirectory = "server-runtime"
           stdoutFileName = 'deploy-server-runtime.log'
           tls = true
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
|akkaSecured|Optional|false|Runs akka communication with worker with enabled TLS TCP. All keys, keystores and truststores are generated per master, worker and satellite.| 
|copyBuildArtifacts|Optional|[:]|Here you can define what would you like to include to integration server from the build process itself. For example you run: `./gradlew build integrationServer` and you create `*.xldp` of your plugin which you would like to include to integration server. You have to specify it here. As for overlay it won't work. With overlay to make it work you have to run 2 commands: `./gradlew build` and then `./gradlew startIntegrationServer`. Key is a relative folder name from Deploy base, and a value is a pattern to all files located in `build` folder except `integration-server` sub-folder. This one is excluded.|
|contextRoot|Optional|/|The context root for Deploy. **Limitation:** *Doesn't work for docker setup*|
|debugPort|Optional|None|Remote Debug Port for Deploy Server| 
|debugSuspend|Optional|false|Suspend the start of the process before the remoting tool is attached.| 
|defaultOfficialPluginsToExclude|Optional|[]|The list of plugins which are going to be excluded from the **plugins/xld-official** before the booting the server. Expected input is the list of strings separated by comma. Exclusion is happening by name convention. If for example you will say "plugin", all plugins going to be removed.| 
|devOpsAsCodes|Optional|None|[Read about this section below](#dev-ops-as-code)|
|dockerImage|Optional|None|When this property is specified, docker based setup will be performed. The name of the docker image, without version. Version is specified in the separate field or dedicated from gradle properties.|
|generateDatasets|Optional|[]|The url `"http://localhost:${server.httpPort}/deployit/generate/${dataset}"` is going to be hit. This URL point is not available in Deploy by default. How you can develop it, is going to be described soon in a blog.|
|httpPort|Optional|Random port|The HTTP port for Deploy server.|
|jvmArgs|Optional|[]|JVM arguments which are going to be used on Server startup|
|logLevels|Optional|[:]|Custom log levels to be included in logback.xml configuration. Expected format is a map, where the key is the package name and value the log level.|
|overlays|Optional|[:]|[Read about this section below](#overlays)|
|pingRetrySleepTime|Optional|10|During the startup of the server we check when it's completely booted. This property configures how long to sleep (in seconds) between retries.|
|pingTotalTries|Optional|60|During the startup of the server we check when it's completely booted. This property configures how many times to retry.|
|runtimeDirectory|Optional|None|When this property is specified, runtime directory setup will be performed. Just make sure that you have complete deploy instance present there.|
|stdoutFileName|Optional|None|The filename that stores standard output and error for server runtime. If not present output is discarded. Note: it should be used only for debugging purposes: if used with class loaded runtime (runtimeDirectory) it will block execution after startup because, for that case, limitations in process spawning |
|version|Optional|None|It can be specified in several ways. Or as a gradle property `xlDeployVersion`, via parameter or in `gradle.properties` file or explicitly via this field.|
|tls|Optional|false|If enabled use HTTPS for API communication with master.|
|yamlPatches|Optional|[:]|[Read about this section below](#yaml-patches)|

### Dev Ops As Code

Applies Dev Ops as code YAML files with extra metadata applied to it (mocked Git metadata associated with the file).

The example of such YAML file is:

```yaml title=devop-as-code-sample.yaml
apiVersion: xl-deploy/v1
kind: Applications
spec:
  - name: Applications/WithGitMetaInfo
    type: udm.Application
    children:
      - name: "1.0.1"
        type: udm.DeploymentPackage
      - name: "1.0.2"
        type: udm.DeploymentPackage

```

Read more about devops as code here: 
[https://docs.xebialabs.com/v.10.2/deploy/how-to/work-with-xl-yaml-format-for-deploy/#yaml-file-fields](https://docs.xebialabs.com/v.10.2/deploy/how-to/work-with-xl-yaml-format-for-deploy/#yaml-file-fields) 

|Name|Type|Default Value|Description|
| :---: | :---: | :---: | :---: |
|devOpAsCodeScript|Mandatory|None|The path to the script.|
|scmAuthor|Mandatory|None|The author of the commit.|
|scmCommit|Mandatory|None|The Git commit hash.|
|scmDate|Mandatory|None|The date of the commit.|
|scmFile|Mandatory|None|The path of the file|
|scmMessage|Mandatory|None|The Git commit message.|
|scmRemote|Mandatory|None|The Git repository URI.|
|scmType|Mandatory|None|SCM type, for example: git, svn, mercury|


### Overlays

With overlays, you can override any file in any folder in Deploy. <br/>
Overlay is a map, where key is a path of the folder, and value - the file which going to be added or overwritten.  

Example: 

```groovy title=build.gradle
overlays = [
   'build/artifacts': ["${ciExplorerDataDependency}:artifacts@zip"],
   conf             : [
       "${ciExplorerDataDependency}:configuration@zip",
       files("src/test/xld/deployit-license.lic")
   ],
   ext              : ["${ciExplorerDataDependency}:extensions@zip"],
   lib              : [project.tasks.getByName("jar").outputs.files],
   'plugins/xld-official': [
       "com.xebialabs.deployit.plugins:xld-ci-explorer:${xldCiExplorerVersion}@xldp", 
   ], 
   'xldrepo'        : ["${ciExplorerDataDependency}:repository@zip"],
]
```

:::caution

With Docker based setup you can override only folders which are mounted from Docker image.
Namely:

 * **conf**
 * **ext**
 * **hotfix/lib**
 * **hotfix/plugins**
 * **plugins**
 * **repository**
 * **work**
 
:::


### YAML patches

Central configuration files are YAML files. There are predefined values there which you might want to custom for your needs,
before starting the integration server. Though the feature is not limited to central configuration files only.
You still can point to any folder and create/overwrite any file you wish. The configuration is a map (key -> map),
where a key is a folder path and value is another map, in which key is the path to the property and value is a value of the property.

For example, if you want to modify in `deploy-client.yaml` file the `automatically-map-all-deployables` to `false`, you have 
to do:

![Yaml Patch Example](../pics/yaml-patch-example.png)

```groovy
yamlPatches = [
   'centralConfiguration/deploy-client.yaml': [
       'deploy.client.automatically-map-all-deployables': 'false'
   ]
]  
```


## Database section

```groovy title=build.gradle
deployIntegrationServer {
   database { 
      derbyPort.set(10000)
      driverVersions.set([
             'mssql'        : '8.4.1.jre8',
             'mysql'        : '8.0.22',
             'mysql-8'      : '8.0.22',
             'oracle-19c-se': '21.1.0.0',
             'postgres-10'  : '42.2.9',
             'postgres-12'  : '42.2.23',
      ])
      logSql.set(true)
   }
}
```

|Name|Type|Default Value|Description|
| :---: | :---: | :---: | :---: |
|derbyPort|Optional|Random number|If derby database is used, the port on which it's going to be started. For other databases port is fixed.|
|driverVersions|Optional|['mssql':'8.4.1.jre8','mysql':'8.0.22','mysql-8':'8.0.22','oracle-19c-se':'21.1.0.0','postgres-10':'42.2.9','postgres-12':'42.2.23']||
|logSql|Optional|false|If true, enables tracing all SQL queries|

The most important what you have to know regarding the database configuration is, choosing which database to run is happening 
now on the level of project property `database`. It means that you can specify it in 2 ways:

* adding a parameter via `-Pdatabase`
* In the root of your project in `gradle.properties` file

```properties title=gradle.properties
database=derby-network
```

If nothing specified, **derby in memory** is going to be used.

## Workers section

Whenever you have to distribute the load of your system and parallelize the deployment execution you can configure workers.
In real case scenario you can run workers as on the same as well as on different VMs.
There are 3 types of workers: 
* internal worker which is embedded to a server, when no workers configured.
* local workers, when extra JVM processed is spanned from the same folder
* external workers, when you run the worker from another folder on the same or different VM
 
Read more about workers here:
[https://docs.xebialabs.com/v.10.2/deploy/concept/high-availability-with-master-worker-setup/#preparing-multiple-masters](https://docs.xebialabs.com/v.10.2/deploy/concept/high-availability-with-master-worker-setup/#preparing-multiple-masters)

**Integration Server** currently support running only on the same VM.

```groovy
deployIntegrationServer {
    workers {
        worker01 {  // The name of the section, you can name it as you with
            version = "10.2.2" // Optional, if not specified will use same version as Server
        }
        worker02 {  // The name of the section, you can name it as you with
            debugPort = 5006
            debugSuspend = true
            jvmArgs = ["-Xmx1024m", "-Duser.timezone=UTC"]
        }
        worker03 {  // The name of the section, you can name it as you with
            debugPort = 5007
            debugSuspend = false
            runtimeDirectory = "/opt/xl-deploy-worker"
            slimDistribution = true 
            stdoutFileName = 'worker.log'
            jvmArgs = ["-Xmx1024m", "-Duser.timezone=UTC"]
            port = 8182
        }
    }
}
```

|Name|Type|Default Value|Description|
| :---: | :---: | :---: | :---: |
|debugPort|Optional|None|Remote Debug Port for a worker.|
|debugSuspend|Optional|None|Suspend the start of the process before the remoting tool is attached.|
|jvmArgs|Optional|None|JVM arguments which are going to be used on a worker startup.|
|logLevels|Optional|[:]|Custom log levels to be included in logback.xml configuration. Expected format is a map, where the key is the package name and value the log level.|
|overlays|Optional|[:]|[Read about this section below](#overlays)|
|port|Optional|None|Port on which worker will start.|
|runtimeDirectory|Optional|None|If specified, it will run external worker, from the different folder location than server. It will not try to download released version.|
|slimDistribution|Optional|false|When false runs worker within cloned xl-deploy directory. If true runs worker from downloaded deploy-task-engine. Default is false. |
|stdoutFileName|Optional|None|The filename that stores standard output and error for worker runtime. If not present output is discarded. Note: it should be used only for debugging purposes: if used with class loaded runtime (runtimeDirectory) it will block execution after startup because, for that case, limitations in process spawning |
|version|Optional|None|It can be specified in several ways. Or as a gradle property `deployTaskEngineVersion`, via parameter or in `gradle.properties` file or explicitly via this field. If not specified, it will take the same version as Server.|

:::caution

Docker based setup currently don't support workers. 

:::

## Satellites section

In comparison with workers, the goal for a satellite is to perform deployments on the different network with Deploy. When
the connection between networks is not fast and less reliable. <br/>

**Integration Server** at this moment doesn't simulate slow network, but rather allows you to test that the functionality 
properly works on a satellite. A satellite itself is installed on the same VM. <br/>

You can read more about a satellite here: 
[https://docs.xebialabs.com/v.10.2/deploy/concept/getting-started-with-the-satellite-module/](https://docs.xebialabs.com/v.10.2/deploy/concept/getting-started-with-the-satellite-module/)

```groovy
deployIntegrationServer {
    satellites {
       satellite01 {  // The name of the section, you can name it as you with
            akkaStreamingPort = 8481
            debugPort = 5008
            debugSuspend = true
            metricsPort = 8081
            overlays = [
                lib              : [files("src/test/resources/my-library.jar")],
                ext              : ["src/test/resources/synthetic.xml"]
            ]      
            serverAkkaBindHostName = "0.0.0.0"
            serverAkkaHostname = "127.0.0.1"
            serverAkkaPort = 8381 
            stdoutFileName = "satellite-startup.log"
            syncPlugins = false
            version = "10.2.2"
       }   
    }
}
```

|Name|Type|Default Value|Description|
| :---: | :---: | :---: | :---: |
|akkaStreamingPort|Optional|8480|Streaming Akka port between Satellite and Server|
|debugPort|Optional|None|Remote Debug Port for a satellite.|
|debugSuspend|Optional|None|Suspend the start of the process before the remoting tool is attached.|
|metricsPort|Optional|8080|Port for gathered (JMX) metrics on Satellite|
|overlays|Optional|[:]|Identical to Server overlays, only in a satellite. [Read about this section below](#overlays)|
|serverAkkaBindHostName|Optional|0.0.0.0|Akka bind host name of the Server|
|serverAkkaHostname|Optional|127.0.0.1|Akka host name of the Server|
|serverAkkaPort|Optional|8380|Akka port between Satellite and Server|
|stdoutFileName|Optional|None|The filename that stores standard output and error for server runtime. If not present output is discarded. |
|syncPlugins|Optional|true|If true, plugins will be synchronized from the server.|
|version|Optional|None|It can be specified in several ways. Or as a gradle property `xlSatelliteVersion`, via parameter or in `gradle.properties` file or explicitly via this field.|

:::caution

Docker based setup currently don't support satellites. 

:::

## MQ Driver Versions

Message Queue Drivers are by default chosen by the plugin, you can change that, and this is exactly what this section about.

That's how your can override it: 

```groovy
deployIntegrationServer {
    mqDriverVersions {
        [
            'activemq': '5.16.2',
            'rabbitmq': '2.2.0'
        ]
    }
}
```

In this sample you can see the default values used in the plugin.

## XLD Integration Server Data Version

Currently, this is used only internally in Digital.ai to point to a package with imported data. <br/>
Before server starts, database is going to be populated by the imported data, to save the time during test run.

## Tests section

You can create Jython based tests and communicate with Deploy through CLI.

To run tests you have to run `./gradlew integrationTests`. It is not a part of `startIntegrationServer` intentionally.
The server start up takes time, especially if also workers and satellites are configured. During development of the tests, you don't want
to reboot it every time, but rather run tests against the configured instance. 

Therefore first you run the server with `./gradlew clean startIntegrationServer` and the you can run multiple times `./gradlew integrationTests`. 

You can also run both commands in one command as: `./gradlew clean startIntegrationServer integrationTests`.

```groovy
deployIntegrationServer {
    tests {
        base {
            base = true
            extraClassPath = [file("src/test/resources")]
            scriptPattern = /\/jython\/ci\/(.+).py$/
        }
        testGroupO1 { // The name of the section, you can name it as you with
            baseDirectory = file("src/test")
            extraClassPath = [file("src/test/resources/group-01")]
            scriptPattern = /\/jython\/ci\/group-01\/(.+).py$/
            setupScripts = ["provision/setup.py", "provision/azure/setup.py"]
            systemProperties = [
                    'key1': 'value1',
                    'key2': 'value2',
            ]
            tearDownScripts = ["provision/azure/teardown.py", "provision/teardown.py"]
        }
    }
}
```

|Name|Type|Default Value|Description|
| :---: | :---: | :---: | :---: |
|base|Optional|`false`|If to define `base` test section, it will be not executable, but sharing the same configuration across all executable test sections. If same property is defined in test section itself, it overrides base defined property.|
|baseDirectory|Mandatory|None|You have to specify here the base directory where your test setup is located.|
|extraClassPath|Optional|[]|You can point to a folder with your Jython utility scripts which you would like to use in other scripts to eliminate code duplication.|
|scriptPattern|Optional|/(.+)[.](py &#124; cli)/|The pattern which will filter the tests you want to run. By default it will run all tests which have extension `py` or `cli` and reside inside base directory.|
|setupScripts|Optional|[]|Provision scripts which will be triggered before running all tests.|
|systemProperties|Optional|[:]|You can provide system properties inside your tests and then access it like `System.getProperty("key1")`|
|tearDownScripts|Optional|[]|As the best practice to clean everything created by test(s), these scripts is exactly the place to do it. It will be triggered regardless if test was successful or not.|
