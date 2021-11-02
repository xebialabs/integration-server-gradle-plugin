"use strict";(self.webpackChunkdocs=self.webpackChunkdocs||[]).push([[220],{3905:function(t,e,r){r.d(e,{Zo:function(){return s},kt:function(){return k}});var a=r(7294);function n(t,e,r){return e in t?Object.defineProperty(t,e,{value:r,enumerable:!0,configurable:!0,writable:!0}):t[e]=r,t}function o(t,e){var r=Object.keys(t);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(t);e&&(a=a.filter((function(e){return Object.getOwnPropertyDescriptor(t,e).enumerable}))),r.push.apply(r,a)}return r}function i(t){for(var e=1;e<arguments.length;e++){var r=null!=arguments[e]?arguments[e]:{};e%2?o(Object(r),!0).forEach((function(e){n(t,e,r[e])})):Object.getOwnPropertyDescriptors?Object.defineProperties(t,Object.getOwnPropertyDescriptors(r)):o(Object(r)).forEach((function(e){Object.defineProperty(t,e,Object.getOwnPropertyDescriptor(r,e))}))}return t}function l(t,e){if(null==t)return{};var r,a,n=function(t,e){if(null==t)return{};var r,a,n={},o=Object.keys(t);for(a=0;a<o.length;a++)r=o[a],e.indexOf(r)>=0||(n[r]=t[r]);return n}(t,e);if(Object.getOwnPropertySymbols){var o=Object.getOwnPropertySymbols(t);for(a=0;a<o.length;a++)r=o[a],e.indexOf(r)>=0||Object.prototype.propertyIsEnumerable.call(t,r)&&(n[r]=t[r])}return n}var d=a.createContext({}),p=function(t){var e=a.useContext(d),r=e;return t&&(r="function"==typeof t?t(e):i(i({},e),t)),r},s=function(t){var e=p(t.components);return a.createElement(d.Provider,{value:e},t.children)},c={inlineCode:"code",wrapper:function(t){var e=t.children;return a.createElement(a.Fragment,{},e)}},m=a.forwardRef((function(t,e){var r=t.components,n=t.mdxType,o=t.originalType,d=t.parentName,s=l(t,["components","mdxType","originalType","parentName"]),m=p(r),k=n,g=m["".concat(d,".").concat(k)]||m[k]||c[k]||o;return r?a.createElement(g,i(i({ref:e},s),{},{components:r})):a.createElement(g,i({ref:e},s))}));function k(t,e){var r=arguments,n=e&&e.mdxType;if("string"==typeof t||n){var o=r.length,i=new Array(o);i[0]=m;var l={};for(var d in e)hasOwnProperty.call(e,d)&&(l[d]=e[d]);l.originalType=t,l.mdxType="string"==typeof t?t:n,i[1]=l;for(var p=2;p<o;p++)i[p]=r[p];return a.createElement.apply(null,i)}return a.createElement.apply(null,r)}m.displayName="MDXCreateElement"},5583:function(t,e,r){r.r(e),r.d(e,{frontMatter:function(){return l},contentTitle:function(){return d},metadata:function(){return p},toc:function(){return s},default:function(){return m}});var a=r(7462),n=r(3366),o=(r(7294),r(3905)),i=["components"],l={sidebar_position:6},d="Tasks",p={unversionedId:"deploy/tasks",id:"deploy/tasks",isDocsHomePage:!1,title:"Tasks",description:"Gradle execute everything with help of tasks.",source:"@site/docs/deploy/tasks.md",sourceDirName:"deploy",slug:"/deploy/tasks",permalink:"/integration-server-gradle-plugin/docs/deploy/tasks",tags:[],version:"current",sidebarPosition:6,frontMatter:{sidebar_position:6},sidebar:"tutorialSidebar",previous:{title:"Configuration",permalink:"/integration-server-gradle-plugin/docs/deploy/configuration"},next:{title:"Flags",permalink:"/integration-server-gradle-plugin/docs/deploy/flags"}},s=[],c={toc:s};function m(t){var e=t.components,r=(0,n.Z)(t,i);return(0,o.kt)("wrapper",(0,a.Z)({},c,r,{components:e,mdxType:"MDXLayout"}),(0,o.kt)("h1",{id:"tasks"},"Tasks"),(0,o.kt)("p",null,"Gradle execute everything with help of tasks. ",(0,o.kt)("br",null),"\nYou can create dependencies between tasks, the order, skip or add some dynamically base on some conditions. ",(0,o.kt)("br",null),"\nHere is the short description for each task in the system, to have an understanding what is going on."),(0,o.kt)("p",null,"In your project can can also call the task directly by its name or skip it from the chain of the task executions,\nby specifying ",(0,o.kt)("inlineCode",{parentName:"p"},"-x *taskName*"),"."),(0,o.kt)("table",null,(0,o.kt)("thead",{parentName:"table"},(0,o.kt)("tr",{parentName:"thead"},(0,o.kt)("th",{parentName:"tr",align:"center"},"Task Name"),(0,o.kt)("th",{parentName:"tr",align:"center"},"Description"))),(0,o.kt)("tbody",{parentName:"table"},(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"applicationConfigurationOverride"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Makes sure that even in case of overlay for ",(0,o.kt)("inlineCode",{parentName:"td"},"deployit.conf")," certain properties are still what user defined. Like HTTP port or HTTP context root.")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"centralConfiguration"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Configures certain central configuration files based on provided data by user, like repository config, workers, etc.")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"checkUILibVersions"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Checks that React, Redux and other libraries are of the same version across all UI Deploy modules.")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"cliCleanDefaultExt"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Removes all default content from ",(0,o.kt)("inlineCode",{parentName:"td"},"ext")," folder. By default it's enabled. If you rely on those python helper scripts, you have to disable it.")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"cliOverlays"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Overlays the files for CLI. ",(0,o.kt)("a",{parentName:"td",href:"/integration-server-gradle-plugin/docs/deploy/configuration#overlays"},"Read more here"))),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"copyCliBuildArtifacts"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Copying artifacts produced inside your project (custom plugin) into CLI folders, which you define yourself.")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"copyOverlays"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Overlays the files for the Deploy server. ",(0,o.kt)("a",{parentName:"td",href:"/integration-server-gradle-plugin/docs/deploy/configuration#overlays"},"Read more here"))),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"copySatelliteOverlays"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Overlays the files for the Satellite.")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"copyServerBuildArtifacts"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Copying artifacts produced inside your project (custom plugin) into Deploy folders, which you define yourself.")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"copyIntegrationServer"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Copy configured integration server to the worker directory.")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"databaseStart"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Starts a database.")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"databaseStop"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Stops a database")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"dockerBasedStopDeploy"),(0,o.kt)("td",{parentName:"tr",align:"center"},"If Deploy was started as a docker container, will stop it and clean all created volumes.")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"downloadAndExtractCli"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Downloads and extracts Cli from a private Nexus.")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"downloadAndExtractDbUnitData"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Downloads and extracts DB Unit Data from a private Nexus.")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"downloadAndExtractSatelliteServer"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Downloads and extracts Satellite archive from a private Nexus.")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"downloadAndExtractServer"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Downloads and extracts Deploy Server archive from a private Nexus.")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"downloadAndExtractWorkerServer"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Downloads and extracts Deploy Worker archive from a private Nexus.")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"exportDatabase"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Exports anonymized data of the database with help of DB Unit to XML format. ",(0,o.kt)("br",null)," ",(0,o.kt)("a",{parentName:"td",href:"https://docs.xebialabs.com/v.10.2/deploy/concept/database-anonymizer/"},"Read more here"))),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"gitlabStart"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Starts the GitLab server in a docker image. Can be used to test ",(0,o.kt)("a",{parentName:"td",href:"https://docs.xebialabs.com/v.10.2/deploy/stitch/introduction-to-stitch/"},"Stitch")," functionality")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"gitlabStop"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Stops the GitLab server in a docker image.")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"importDbUnitData"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Imports data into a database")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"integrationTests"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Runs Jython integration tests via CLI. You can define certain patterns and use Gradle flags to narrow down the scope of running tests.")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"prepareDeploy"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Creates initial folders and ",(0,o.kt)("inlineCode",{parentName:"td"},"deployit.conf")," file")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"prepareDatabase"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Copies required DB specific driver and configures ",(0,o.kt)("inlineCode",{parentName:"td"},"deploy-repository.yaml")," in ",(0,o.kt)("inlineCode",{parentName:"td"},"centralConfiguration"))),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"prepareSatellites"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Changes ports in satellite.conf specified by the user")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"runCli"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Runs CLI as a process")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"runDatasetGeneration"),(0,o.kt)("td",{parentName:"tr",align:"center"},"The url ",(0,o.kt)("inlineCode",{parentName:"td"},'"http://localhost:${server.httpPort}/deployit/generate/${dataset}"')," is going to be hit. This URL point is not available in Deploy by default. How you can develop it, is going to be described soon in a blog.")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"runDevOpsAsCode"),(0,o.kt)("td",{parentName:"tr",align:"center"},(0,o.kt)("a",{parentName:"td",href:"/integration-server-gradle-plugin/docs/deploy/configuration#dev-ops-as-code"},"Read about it here"))),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"runProvisionScript"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Starts the server and runs the provision script. You might need it if you would like to provision the test server prior to running tests.")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"satelliteOverlays"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Overlays the files for the Satellite.")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"setLogbackLevels"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Modifies the ",(0,o.kt)("inlineCode",{parentName:"td"},"logback.xml")," by amending the levels of logs for specified packages.")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"setWorkerLogbackLevels"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Modifies the ",(0,o.kt)("inlineCode",{parentName:"td"},"logback.xml")," by amending the levels of logs for specified packages.")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"shutdownMq"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Shut downs docker image with MQ")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"shutdownIntegrationServer"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Shutdown a integration server and all dependencies: workers, mq, satellite, etc.")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"shutdownSatellite"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Shutdown a satellite.")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"shutdownWorkers"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Shutdown a worker.")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"startIntegrationServer"),(0,o.kt)("td",{parentName:"tr",align:"center"},"The entry point for the plugin, which starts the integration server with all dependencies.")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"startMq"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Starts MQ in a docker image.")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"startPluginManager"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Starts the plugin manager. You have to have a CLI for that.")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"startSatellite"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Starts the satellite as JDK process.")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"startWorkers"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Starts the worker as JDK process.")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"syncServerPluginsWithWorker"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Copy all plugins from the xl-deploy to the worker runtime directory.")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"yamlPatch"),(0,o.kt)("td",{parentName:"tr",align:"center"},(0,o.kt)("a",{parentName:"td",href:"/integration-server-gradle-plugin/docs/deploy/configuration#yaml-patches"},"Read about it here"))),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"workerOverlays"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Overlays the files for the Worker.")))))}m.isMDXComponent=!0}}]);