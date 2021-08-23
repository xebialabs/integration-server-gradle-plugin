---
sidebar_position: 5
---

# Tasks

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
