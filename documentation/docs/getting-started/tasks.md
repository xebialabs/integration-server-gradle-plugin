---
sidebar_position: 6
---

# Tasks

Gradle execute everything with help of tasks. <br/>
You can create dependencies between tasks, the order, skip or add some dynamically base on some conditions. <br/>
Here is the short description for each task in the system, to have an understanding what is going on.

In your project can can also call the task directly by its name or skip it from the chain of the task executions,
by specifying `-x *taskName*`.

|Task Name|Description|
| :---: | :---: |
|applicationConfigurationOverride|Makes sure that even in case of overlay for `deployit.conf` certain properties are still what user defined. Like HTTP port or HTTP context root.|
|centralConfiguration|Configures certain central configuration files based on provided data by user, like repository config, workers, etc.|
|checkUILibVersions|Checks that React, Redux and other libraries are of the same version across all UI Deploy modules.|
|cliCleanDefaultExt|Removes all default content from `ext` folder. By default it's enabled. If you rely on those python helper scripts, you have to disable it.|
|cliOverlays|Overlays the files for CLI. [Read more here](./configuration.md#overlays)|
|copyCliBuildArtifacts|Copying artifacts produced inside your project (custom plugin) into CLI folders, which you define yourself.|
|copyOverlays|Overlays the files for the Deploy server. [Read more here](./configuration.md#overlays)|
|copySatelliteOverlays|Overlays the files for the Satellite.|
|copyServerBuildArtifacts|Copying artifacts produced inside your project (custom plugin) into Deploy folders, which you define yourself.|
|copyIntegrationServer|Copy configured integration server to the worker directory.|
|databaseStart|Starts a database.| 
|databaseStop|Stops a database| 
|dockerBasedStopDeploy|If Deploy was started as a docker container, will stop it and clean all created volumes.|
|downloadAndExtractCli|Downloads and extracts Cli from a private Nexus.|
|downloadAndExtractDbUnitData|Downloads and extracts DB Unit Data from a private Nexus.|
|downloadAndExtractSatelliteServer|Downloads and extracts Satellite archive from a private Nexus.|
|downloadAndExtractServer|Downloads and extracts Deploy Server archive from a private Nexus.|
|downloadAndExtractWorkerServer|Downloads and extracts Deploy Worker archive from a private Nexus.|
|exportDatabase|Exports anonymized data of the database with help of DB Unit to XML format. <br/> [Read more here](https://docs.xebialabs.com/v.10.2/deploy/concept/database-anonymizer/)|
|gitlabStart|Starts the GitLab server in a docker image. Can be used to test [Stitch](https://docs.xebialabs.com/v.10.2/deploy/stitch/introduction-to-stitch/) functionality|
|gitlabStop|Stops the GitLab server in a docker image.| 
|importDbUnitData|Imports data into a database|
|integrationTests|Runs Jython integration tests via CLI. You can define certain patterns and use Gradle flags to narrow down the scope of running tests.|
|prepareDeploy|Creates initial folders and `deployit.conf` file| 
|prepareDatabase|Copies required DB specific driver and configures `deploy-repository.yaml` in `centralConfiguration`|
|runCli|Runs CLI as a process| 
|runDatasetGeneration|The url `"http://localhost:${server.httpPort}/deployit/generate/${dataset}"` is going to be hit. This URL point is not available in Deploy by default. How you can develop it, is going to be described soon in a blog.|
|runDevOpsAsCode|[Read about it here](./configuration.md#dev-ops-as-code)|
|runProvisionScript|Starts the server and runs the provision script. You might need it if you would like to provision the test server prior to running tests.|
|satelliteOverlays|Overlays the files for the Satellite.|
|setLogbackLevels|Modifies the `logback.xml` by amending the levels of logs for specified packages.|
|setWorkerLogbackLevels|Modifies the `logback.xml` by amending the levels of logs for specified packages.|
|shutdownMq|Shut downs docker image with MQ| 
|shutdownIntegrationServer|Shutdown a integration server and all dependencies: workers, mq, satellite, etc.|
|shutdownSatellite|Shutdown a satellite.|
|shutdownWorkers|Shutdown a worker.|
|startIntegrationServer|The entry point for the plugin, which starts the integration server with all dependencies.|
|startMq|Starts MQ in a docker image.|
|startPluginManager|Starts the plugin manager. You have to have a CLI for that.|
|startSatellite|Starts the satellite as JDK process.|
|startWorkers|Starts the worker as JDK process.|
|syncServerPluginsWithWorker|Copy all plugins from the xl-deploy to the worker runtime directory.|
|yamlPatch|[Read about it here](./configuration.md#yaml-patches)|
|workerOverlays|Overlays the files for the Worker.|
