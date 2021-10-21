---
sidebar_position: 1
---

# Configuration

## The first section level

```groovy title=build.gradle
releaseIntegrationServer {
    servers {}
}
```

|Name|Description|
| :---: | :---: |
|servers|Server configurations, currently, it's possible to configure only one.|

## Servers section

```groovy title=build.gradle
releaseIntegrationServer {
   servers {
       controlPlane { // The name of the section, you can name it as you wish
           dockerImage = "xebialabs/xl-release" 
           httpPort = 5516
           pingRetrySleepTime = 5
           pingTotalTries = 120
           version = '10.2.2'
       }       
   }   
}
```

|Name|Type|Default Value|Description|
| :---: | :---: | :---: | :---: |
|dockerImage|Optional|None|When this property is specified, docker based setup will be performed. The name of the docker image, without version. Version is specified in the separate field or dedicated from gradle properties.|
|httpPort|Optional|Random port|The HTTP port for Deploy server.|
|pingRetrySleepTime|Optional|10|During the startup of the server we check when it's completely booted. This property configures how long to sleep (in seconds) between retries.|
|pingTotalTries|Optional|60|During the startup of the server we check when it's completely booted. This property configures how many times to retry.|
|version|Optional|None|It can be specified in several ways. Or as a gradle property `xlDeployVersion`, via parameter or in `gradle.properties` file or explicitly via this field.|
