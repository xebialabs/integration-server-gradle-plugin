---
title: How to run a Deploy Cluster on Aws OpenShift setup with help of operator 
tags: [cluster-operator]
---

## Requirements

Documentation is applicable for a version **10.4.0-1209.942** or later.

## Pre-requisites

There are a couple of prerequisites which have to be performed in order to run the automation.
You have to:
* [create a cluster itself on AWS OpenShift](https://docs.openshift.com/rosa/rosa_getting_started/rosa-creating-cluster.html).
* [install oc on your machine](https://docs.openshift.com/container-platform/4.2/cli_reference/openshift_cli/getting-started-cli.html). 
* add to your `~/.gradle/properties` 2 values:
```shell script
ocLogin=...
ocPassword=... 
```
* [create EFS class storage](https://docs.openshift.com/container-platform/4.2/storage/persistent_storage/persistent-storage-efs.html)

## How the full flow works

* Installing a docker based Deploy instance, because we will use Deploy to create necessary resources in kubernetes and to deploy an operator.
* Checking out [Deploy operator](https://github.com/xebialabs/xl-deploy-kubernetes-operator) and modifying the configuration based on the user input
* Installing [XL CLI](https://docs.xebialabs.com/v.10.3/deploy/how-to/install-the-xl-cli/) to apply YAML files 
* Verifying that deployment was successful and all required resources were created in kubernetes. If something went wrong, you'll be notified about it in logs.

You can also check this [documentation](https://xebialabs.github.io/xl-deploy-kubernetes-operator/docs/manual/openshift) for 
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
            activeProviderName = "aws-openshift"
            awsOpenshift {
                apiServerURL = 'https://api.yourhost.lnfl.p1.openshiftapps.com:6443'
                host = 'router-default.apps.yourhost.lnfl.p1.openshiftapps.com'
                name = 'aws-openshift-test-cluster'
                oauthHostName = "oauth-openshift.apps.yourhost.lnfl.p1.openshiftapps.com"
                operatorImage = 'acierto/deploy-operator:1.0.6-openshift'
                operatorPackageVersion = "1.0.1"
            }
        }
    }
    servers {
        server01 {
            dockerImage = "xebialabsunsupported/xl-deploy"
            pingRetrySleepTime = 10
            pingTotalTries = 120
            version = "${xlDeployTrialVersion}"
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
 it will create 2 masters and 2 workers. The final URL to connect to UI is: `router-default.apps.yourhost.lnfl.p1.openshiftapps.com`.
 In case if you want to update the operator and use your own, you can change `operatorImage`. As you can see from this 
 example, that's exactly what happened. `acierto/deploy-operator:1.0.6-openshift` is not the official operator.
 Information about `apiServerURL`, `host` and `oauthHostName` you should check in your OpenShift Cluster console. 
