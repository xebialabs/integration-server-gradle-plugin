---
title: How to run a Deploy Cluster on Minikube local setup with help of operator 
tags: [cluster-operator]
---

## Requirements

Documentation is applicable for a version **10.4.0-1209.942** or later.

## Pre-requisites

There are a couple of prerequisites which have to be performed in order to run the automation.
You have to:
* [install minikube on your machine](https://minikube.sigs.k8s.io/docs/start/).
* the plugin is using [virtualbox driver](https://minikube.sigs.k8s.io/docs/drivers/virtualbox/)
  so [install virtualbox on your machine](https://www.virtualbox.org/wiki/Downloads). 

## How the full flow works

* Installing a docker based Deploy instance, because we will use Deploy to create necessary resources in kubernetes and to deploy an operator.
* Checking out [Deploy operator](https://github.com/xebialabs/xl-deploy-kubernetes-operator) and modifying the configuration based on the user input
* Installing [XL CLI](https://docs.xebialabs.com/v.10.3/deploy/how-to/install-the-xl-cli/) to apply YAML files 
* Verifying that deployment was successful and all required resources were created in kubernetes. If something went wrong, you'll be notified about it in logs.

You can also check this [documentation](https://xebialabs.github.io/xl-deploy-kubernetes-operator/docs/manual/onprem) for more information.

All of this is automated and can be triggered by `./gradlew clean :core:startIntegrationServer --stacktrace` with the configuration that is similar 
to the following example.

:::note

During cluster setup there will be info line, similar to the following:
"Please enter your password if requested for user ${current_username} or give user sudoers permissions '${current_username} ALL=(ALL) NOPASSWD: ${path_to_script}/update_etc_hosts.sh'."
where placeholders ${current_username} and ${path_to_script} will be replaced with correct values.

Update your `/etc/sudoers` file according to the info line that will be provided in the console log. If you didn't do that on time:
- stop current build and rerun full installation after update of `/etc/sudoers`
- or run following script: `sudo "${path_to_script}/update_etc_hosts.sh"`

:::

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
            activeProviderName = "onprem"
            onPremise {
                name = 'onprem-test-cluster'
                clusterNodeCpus = 4
                clusterNodeMemory = 15000
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
 it will create 2 masters and 2 workers. The final URL to connect to UI is: `http://onprem-test-cluster.digitalai-testing.com/xl-deploy/#/explorer`.
In case if you want to update the operator and use your own, you can change `operatorImage`.
Cluster will use virtualbox with 4 CPUs and 15000MB memory.
