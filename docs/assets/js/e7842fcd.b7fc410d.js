"use strict";(self.webpackChunkdocs=self.webpackChunkdocs||[]).push([[522],{8078:function(e,t,r){r.r(t),r.d(t,{frontMatter:function(){return l},contentTitle:function(){return s},metadata:function(){return d},toc:function(){return p},default:function(){return m}});var n=r(7462),i=r(3366),a=(r(7294),r(3905)),o=["components"],l={sidebar_position:4},s="API",d={unversionedId:"getting-started/api",id:"getting-started/api",isDocsHomePage:!1,title:"API",description:"Configuration",source:"@site/docs/getting-started/api.md",sourceDirName:"getting-started",slug:"/getting-started/api",permalink:"/integration-server-gradle-plugin/docs/getting-started/api",version:"current",sidebarPosition:4,frontMatter:{sidebar_position:4},sidebar:"tutorialSidebar",previous:{title:"Examples",permalink:"/integration-server-gradle-plugin/docs/getting-started/examples"},next:{title:"Limitations",permalink:"/integration-server-gradle-plugin/docs/getting-started/limitations"}},p=[{value:"Configuration",id:"configuration",children:[]},{value:"Tasks",id:"tasks",children:[]},{value:"Flags",id:"flags",children:[]}],u={toc:p};function m(e){var t=e.components,r=(0,i.Z)(e,o);return(0,a.kt)("wrapper",(0,n.Z)({},u,r,{components:t,mdxType:"MDXLayout"}),(0,a.kt)("h1",{id:"api"},"API"),(0,a.kt)("h2",{id:"configuration"},"Configuration"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-groovy"},'integrationServer {\n    servers { // for now it can be only one server\n        controlPlane {\n            contextRoot = "/custom" // By default "/", but you can customize it\n            debugPort = 4005 // Debug port, by default it is disabled\n            debugSuspend = true // by default false\n            dockerImage = "xebialabs/xl-deploy" // If that field is filled in, server will be started as a container. Note that container setup has limited features. \n            httpPort = 4516 // Server HTTP port, by default it is random port\n            jvmArgs = ["-Xmx1024m", "-Duser.timezone=UTC"] // custom Java process arguments, only works if to run the server from runtime directory\n            logLevels = ["com.xebialabs.deployit.plugin.stitch": "debug"] // Log level overwrites\n            overlays = [\n                plugins          : [\n                    "com.xebialabs.deployit.plugins:xld-ci-explorer:${xldCiExplorerVersion}@xldp", \n                ], // List of plugins to install \n                stitch           : ["${ciExplorerDataDependency}:stitch@zip"], // Creates a folder "stitch" with copied content of zip archive \n                conf             : [\n                    "${ciExplorerDataDependency}:configuration@zip",\n                    files("src/test/xld/deployit-license.lic")\n                ], // Additional configuration files, e.g. license or archived configuration files\n                lib              : [project.tasks.getByName("jar").outputs.files], // List of libraries to install in lib directory\n                ext              : ["${ciExplorerDataDependency}:extensions@zip"], // List of extensions to install\n                \'derbydb/xldrepo\': ["${ciExplorerDataDependency}:repository@zip"], // Derby data files, if Derby is used\n                \'build/artifacts\': ["${ciExplorerDataDependency}:artifacts@zip"], // List of artifacts to import\n            ]\n            runtimeDirectory = "server-runtime" // If to specify this directory, Deploy will be started from this folder and will not download it from external provider (Nexus)\n            version = \'10.2.0\' // Version of the Server. By default it takes it from project property `xlDeployVersion`.\n            yamlPatches = [ // Overwrites YAML file properties (create the file if it didn\'t exist yet)\n                \'centralConfiguration/deploy-server.yaml\': [\n                    \'deploy.server.hostname\': \'test.xebialabs.com\',\n                    \'deploy.server.label\': \'XLD\'\n                ]\n            ]     \n        }       \n    }   \n    \n    workers {\n        // By default we need only name, debugPort is disabled and port will be auto-generated from free ports\n        // if directory is not specified then we run worker from the xl-deploy-server as local worker.\n        // if directory is specified, then value should be absolute path\n        worker01 { // name = worker01, worker01 will start from the same server directory as local worker(xl-deploy-10.2.0-server)\n        }\n        worker02 { // name = worker02, worker02 will start from the same server directory as local worker (xl-deploy-10.2.0-server)\n            debugPort = 5006  // Debug port, by default it is disabled\n            debugSuspend = true // by default false\n            jvmArgs = ["-Xmx1024m", "-Duser.timezone=UTC"]\n        }\n        worker03 { // name = worker03, worker03 will start from the mentioned directory path(/opt/xl-deploy-worker)\n            debugPort = 5007  // Debug port, by default it is disabled\n            directory = "/opt/xl-deploy-worker"\n            debugSuspend = false\n            jvmArgs = ["-Xmx1024m", "-Duser.timezone=UTC"]\n            port = 8182\n        }\n    }\n\n    satellites {\n       satellite01 {\n            debugPort = 5008  // Debug port, by default it is disabled\n            debugSuspend = true // By default false\n       }   \n    }\n}\n')),(0,a.kt)("h2",{id:"tasks"},"Tasks"),(0,a.kt)("ul",null,(0,a.kt)("li",{parentName:"ul"},(0,a.kt)("p",{parentName:"li"},(0,a.kt)("inlineCode",{parentName:"p"},"dockerComposeDatabaseStart")," - starts containers required by the server")),(0,a.kt)("li",{parentName:"ul"},(0,a.kt)("p",{parentName:"li"},(0,a.kt)("inlineCode",{parentName:"p"},"dockerComposeDatabaseStop")," - stops containers required by the server")),(0,a.kt)("li",{parentName:"ul"},(0,a.kt)("p",{parentName:"li"},(0,a.kt)("inlineCode",{parentName:"p"},"ImportDbUnitDataTask")," - imports data files into a database")),(0,a.kt)("li",{parentName:"ul"},(0,a.kt)("p",{parentName:"li"},(0,a.kt)("inlineCode",{parentName:"p"},"prepareDatabase")," - copies configuration files for the selected database")),(0,a.kt)("li",{parentName:"ul"},(0,a.kt)("p",{parentName:"li"},(0,a.kt)("inlineCode",{parentName:"p"},"startIntegrationServer")," "),(0,a.kt)("ul",{parentName:"li"},(0,a.kt)("li",{parentName:"ul"},(0,a.kt)("p",{parentName:"li"},"starts an integration server with a provided configuration and a database.")),(0,a.kt)("li",{parentName:"ul"},(0,a.kt)("p",{parentName:"li"},"if the integrationServer needs to be started with the external worker ,we need to add the below configuration in build.gradle. if not integration server will start with in-process-worker."),(0,a.kt)("pre",{parentName:"li"},(0,a.kt)("code",{parentName:"pre",className:"language-grovvy"},'workers {      \n     worker03 { // name = worker03, worker03 will start from the mentioned directory path(/opt/xl-deploy-worker)\n         debugPort = 5007\n         directory = "/opt/xl-deploy-worker"\n         debugSuspend = false\n         jvmArgs = ["-Xmx1024m", "-Duser.timezone=UTC"]\n         port = 8182\n     }\n }\n'))))),(0,a.kt)("li",{parentName:"ul"},(0,a.kt)("p",{parentName:"li"},(0,a.kt)("inlineCode",{parentName:"p"},"shutdownIntegrationServer")," - stops a database server and also stop a database")),(0,a.kt)("li",{parentName:"ul"},(0,a.kt)("p",{parentName:"li"},(0,a.kt)("inlineCode",{parentName:"p"},"startSatellite")," - starts satellite.")),(0,a.kt)("li",{parentName:"ul"},(0,a.kt)("p",{parentName:"li"},(0,a.kt)("inlineCode",{parentName:"p"},"shutdownSatellite")," - stops satellite."))),(0,a.kt)("h2",{id:"flags"},"Flags"),(0,a.kt)("ul",null,(0,a.kt)("li",{parentName:"ul"},(0,a.kt)("inlineCode",{parentName:"li"},"-Pdatabase")," - sets a database to launch, options: ",(0,a.kt)("inlineCode",{parentName:"li"},"derby-inmemory"),", ",(0,a.kt)("inlineCode",{parentName:"li"},"derby-network"),", ",(0,a.kt)("inlineCode",{parentName:"li"},"mssql"),", ",(0,a.kt)("inlineCode",{parentName:"li"},"mysql"),", ",(0,a.kt)("inlineCode",{parentName:"li"},"mysql-8"),", ",(0,a.kt)("inlineCode",{parentName:"li"},"oracle-19c-se"),", ",(0,a.kt)("inlineCode",{parentName:"li"},"postgres")),(0,a.kt)("li",{parentName:"ul"},(0,a.kt)("inlineCode",{parentName:"li"},"-PderbyPort")," - provides Derby port if Derby database is used"),(0,a.kt)("li",{parentName:"ul"},(0,a.kt)("inlineCode",{parentName:"li"},"-PlogSql")," - enables printing of SQL queries executed by the server"),(0,a.kt)("li",{parentName:"ul"},(0,a.kt)("inlineCode",{parentName:"li"},"-PserverDebugPort")," - provides a server debug port for remote debugging"),(0,a.kt)("li",{parentName:"ul"},(0,a.kt)("inlineCode",{parentName:"li"},"-PsatelliteDebugPort")," - provides a satellite debug port for remote debugging"),(0,a.kt)("li",{parentName:"ul"},(0,a.kt)("inlineCode",{parentName:"li"},"-PserverHttpPort")," - provides an http port, overrides a configuration option")))}m.isMDXComponent=!0}}]);