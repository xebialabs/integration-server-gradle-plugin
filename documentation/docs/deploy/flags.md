---
sidebar_position: 7
---

# Flags

Most of the flags (apart from database) you can define in configuration section.<br/>
So why then we need flags?<br/>

You can look at them as phantom configuration, which you don't want to have in your permanent configuration.<br/>
Or it can be used to not create a branching logic for databases. If we would keep it only in a configuration section,
and you would like to run your tests against 3-5 databases, then you had to create some branching logic for that. <br/>
And anyway, you can say your options to Gradle only via parameters. But I agree, that having a default database in 
a configuration section make sense, and we have it in plans to implement.

:::note

Parameters/Flags can be defined in 2 ways:

* In a command line: `./gradlew startIntegrationServer -Pdatabase=postgres-10`
* In `gradle.properties` in a root of your project, as a key value pair: `database=postgres-10`

:::

|Flag name|Options|Description|
| :---: | :---: | :---: |
|clusterMode|`default`<br/> `full`<br/> `hot-standby`<br/>|Specify the cluster mode in which the server needs to start. Cluster tests will be executed based on clusterMode. If not set cluster mode is `default`|
|database|`h2`<br/> `mssql`<br/> `mysql`<br/> `mysql-8`<br/> `oracle-19c-se`<br/> `postgres-10`<br/> `postgres-12`|Type of database. [More details](#database-flag)|
|debug|Boolean|Enables or disables starting processes in debug mode. It overrides any debug setting in configuration. If not set debugging is enabled.|
|databasePort|Any available port|The port on which database is going to be started.|
|logSql|true/false|Enables printing of SQL queries executed on the server|
|satelliteDebugPort|Any available port|Provides a satellite debug port for remote debugging.|
|serverDebugPort|Any available port|Provides a server debug port for remote debugging.|
|serverHttpPort|Any available port|Overrides default server HTTP port|
|testBaseSubDirectory|String|Example: `-PtestBaseSubDirectory=provision-aws` It points to a subset of tests running in a group of the tests|
|testName|String|Example: `-PtestName=azure`. If to specify this parameter, only 1 test section will be executed. It can be helpful, if you run in your CI pipeline tests in parallel and define in each the group of tests to run.|
|testScriptPattern|Pattern|Example: `-PtestScriptPattern=provision-aws/provision_aws.py`|
|testSetupScripts|String|Example: `-PtestScriptPatterns=root/setup.py,root/gcp/setup.py`|
|testTeardownScripts|String|Example: `-PtestTeardownScripts=root/teardown.py,root/gcp/teardown.py`|

:::info

In case when the configuration is defined in `build.gradle` and a parameter provided, parameter will take a precedence. 

:::

## Database flag

Each database configuration is fixed, you can't modify it through the configuration of flags. <br/>
We have plans to make it possible to choose the port for any database. <br/>

What exactly is configured for the database, you can check in `src/main/resources/database-conf`, or when you run the 
*Integration Server* in your `<DEPLOY_HOME>/centralConfiguration/deploy-repository.yaml` file.

### PostgreSQL

PostgreSQL is the default and recommended database for both development and production use.
If no database is specified, PostgreSQL 12 (`postgres-12`) is used.
