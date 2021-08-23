---
sidebar_position: 1
---

# Introduction

Are you developing your own plugin for Deploy and looking for a simple way to run integration tests against it? <br/>
Then you are on the right track! <br/>


**Integration Server** will help you to setup and run Deploy of your preferred version (10.2+) in different ways, by: <br/>
* downloading it from your private Nexus repository
* Running from the specified folder 
* pulling a docker image. 

You can:

* customise the any files
* add libraries and plugins you wish
* do easy YAML patches for central configuration files
* run workers and satellites (and also in a debug mode) 

In only a matter of describing your setup in declarative way. 
The heavy lifting is done by the plugin and about to get more features and help in a mid/long term. 
 
If happens that currently you are missing some feature, it's not a problem, you can extend it. <br/>
Because **Integration Server** built as a Gradle plugin and very flexible for adjustments. 
Moreover, code is open, and you can fork it, or create PRs which we will review and might consider merging it,
 if it is done generic and can be useful for other users/customers too. 
