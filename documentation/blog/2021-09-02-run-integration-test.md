---
title: How to run a simple integration test
tags: [integration-test]
---

Documentation is applicable for a version **10.3.0-902.1028** or later.

The version of the plugin contains not random values, but you can read it next way
10.3.0 means that it works for Deploy 10.3.0

After minus the information of the time when it was released: <br/>
902 - 2d of September <br/>
1020 - 10:20 AM <br/>

I expect that you are the beginner of using this plugin, and your intention is to create your first integration test
for your custom plugin in against running Deploy instance. 

At this moment you can run only Jython based tests which are executed via CLI.

```groovy
integrationServer {
    servers {
        controlPlane {
            dockerImage = "xebialabs/xl-deploy"
            version = "10.2.2"
            httpPort = 4516
            yamlPatches = [
                'centralConfiguration/deploy-server.yaml': [
                    'deploy.server.label': 'Deploy Hello World'
                ]
            ]
        }
    }
    tests {
        myTestScenario1 {
            baseDirectory = file("src/test")
        }
    }
}
```
