---
title: How to run a Deploy Cluster on Azure AKS setup with help of operator 
tags: [cluster-operator]
---

## Requirements

Documentation is applicable for a version **10.4.0-1209.942** or later.

## Pre-requisites

There are a couple of prerequisites which have to be performed in order to run the automation.
You have to:
* You should install [Azure CLI locally](https://docs.microsoft.com/en-us/cli/azure/install-azure-cli)
* add to your `~/.gradle/properties` 2 values:
```shell script
azUsername=...
azPassword=... 
```

## How the full flow works

* Installing a docker based Deploy instance, because we will use Deploy to create necessary resources in kubernetes and to deploy an operator.
* Checking out [Deploy operator](https://github.com/xebialabs/xl-deploy-kubernetes-operator) and modifying the configuration based on the user input
* Installing [XL CLI](https://docs.xebialabs.com/v.10.3/deploy/how-to/install-the-xl-cli/) to apply YAML files 
* Verifying that deployment was successful and all required resources were created in kubernetes. If something went wrong, you'll be notified about it in logs.

You can also check this [operator Azure AKS documentation](https://xebialabs.github.io/xl-deploy-kubernetes-operator/docs/manual/azure-aks) for 
more information.

All of this is automated and can be triggered by `./gradlew clean :core:startIntegrationServer --stacktrace` with the configuration that is similar
to the following example.

When you would like to stop your cluster you can run `./gradlew  :core:shutdownIntegrationServer --stacktrace`.
It will undeploy all CIs, remove all deployed resources on kubernetes and clean all created PVC.

## Example

An example for a complete configuration:

```groovy
deployIntegrationServer {
    cli {
        overlays = [
                ext: [
                        fileTree(dir: "$rootDir/config/cli", includes: ["**/*.py"])
                ],
                lib: [
                        "com.xebialabs.xl-platform.test-utils:py-modules:${testUtilsVersion}@jar"
                ]
        ]
    }
    cluster {
        enable = true
        profile = 'operator'
        publicPort = 10001
    }
    clusterProfiles {
        operator {
            activeProviderName = "azure-aks"
            azureAks {
                name = 'azure-aks-test-cluster'
                clusterNodeCount = 3
                clusterNodeVmSize = 'Standard_DS2_v2'
                location = 'northcentralus'
            }
        }
    }
    servers {
        server01 {
            dockerImage = "xebialabsunsupported/xl-deploy"
            pingRetrySleepTime = 10
            pingTotalTries = 120
            version = "${xlDeployTrialVersion}"
            overlays = [
                    conf: [
                            fileTree(dir: "$rootDir/config/conf", includes: ["*.*"])
                    ],
            ]
        }
        server02 {
        }
    }
    workers {
        worker01 {
            dockerImage = "xebialabsunsupported/deploy-task-engine"
        }
        worker02 {
        }
    }
}
```

The cluster will be created with amount of servers and workers specified in the configuration. For this case,
 it will create 2 masters and 2 workers. The final URL to connect to UI is: 
 `http://azure-aks-test-cluster.northcentralus.cloudapp.azure.com/xl-deploy/#/explorer` (composed of operator provider name and location).
In case if you want to update the operator and use your own, you can change `operatorImage`. 
Cluster will create with 3 cluster nodes with default node-vm-size `Standard_DS2_v2` with 7GiB and 2vCPU. 
The location of the cluster will be `northcentralus`, check with `az account list-locations` for other location.
