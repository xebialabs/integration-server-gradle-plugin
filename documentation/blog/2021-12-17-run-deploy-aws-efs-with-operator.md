---
title: How to run a Deploy Cluster on AWS EKS setup with help of operator 
tags: [cluster-operator]
---

## Requirements

Documentation is applicable for a version **10.4.0-1209.942** or later.

## Pre-requisites

Below are the prerequisites for the automation to run.
You need to:
* Install [AWS CLI locally](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html) and [Configure AWS cli](https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-quickstart.html)
* Install [EKSCTL locally](https://docs.aws.amazon.com/eks/latest/userguide/eksctl.html)
* Install [HELM locally](https://helm.sh/docs/intro/install/)
* Note:  
  * AWS access key and secret key should be configured either via aws configure or using gradle project properties.
  '-PaccessKey= -PsecretKey='  

## How the full flow works

* Installing a docker based Deploy instance, because we will use Deploy to create necessary resources in kubernetes and to deploy an operator.
* Checking out [Deploy operator](https://github.com/xebialabs/xl-deploy-kubernetes-operator) and modifying the configuration based on the user input
* Installing [XL CLI](https://docs.xebialabs.com/v.10.3/deploy/how-to/install-the-xl-cli/) to apply YAML files 
* Verifying that deployment was successful and all required resources were created in kubernetes. If something went wrong, you'll be notified about it in logs.

You can also check this [operator AWS EKS documentation](https://xebialabs.github.io/xl-deploy-kubernetes-operator/docs/manual/aws-eks) for 
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
            activeProviderName = "aws-eks"            
            awsEks {
                        name = 'aws-eks-test-cluster'
                        region = 'us-east-1'
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
 ` http://deploy.digitalai-testing.com/xl-deploy/#/explorer`.
In case if you want to update the operator and use your own, you can change `operatorImage`.
