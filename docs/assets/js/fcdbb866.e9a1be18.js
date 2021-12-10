"use strict";(self.webpackChunkdocs=self.webpackChunkdocs||[]).push([[4493],{3905:function(e,t,r){r.d(t,{Zo:function(){return u},kt:function(){return m}});var n=r(7294);function o(e,t,r){return t in e?Object.defineProperty(e,t,{value:r,enumerable:!0,configurable:!0,writable:!0}):e[t]=r,e}function a(e,t){var r=Object.keys(e);if(Object.getOwnPropertySymbols){var n=Object.getOwnPropertySymbols(e);t&&(n=n.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),r.push.apply(r,n)}return r}function l(e){for(var t=1;t<arguments.length;t++){var r=null!=arguments[t]?arguments[t]:{};t%2?a(Object(r),!0).forEach((function(t){o(e,t,r[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(r)):a(Object(r)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(r,t))}))}return e}function i(e,t){if(null==e)return{};var r,n,o=function(e,t){if(null==e)return{};var r,n,o={},a=Object.keys(e);for(n=0;n<a.length;n++)r=a[n],t.indexOf(r)>=0||(o[r]=e[r]);return o}(e,t);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);for(n=0;n<a.length;n++)r=a[n],t.indexOf(r)>=0||Object.prototype.propertyIsEnumerable.call(e,r)&&(o[r]=e[r])}return o}var s=n.createContext({}),p=function(e){var t=n.useContext(s),r=t;return e&&(r="function"==typeof e?e(t):l(l({},t),e)),r},u=function(e){var t=p(e.components);return n.createElement(s.Provider,{value:t},e.children)},c={inlineCode:"code",wrapper:function(e){var t=e.children;return n.createElement(n.Fragment,{},t)}},f=n.forwardRef((function(e,t){var r=e.components,o=e.mdxType,a=e.originalType,s=e.parentName,u=i(e,["components","mdxType","originalType","parentName"]),f=p(r),m=o,d=f["".concat(s,".").concat(m)]||f[m]||c[m]||a;return r?n.createElement(d,l(l({ref:t},u),{},{components:r})):n.createElement(d,l({ref:t},u))}));function m(e,t){var r=arguments,o=t&&t.mdxType;if("string"==typeof e||o){var a=r.length,l=new Array(a);l[0]=f;var i={};for(var s in t)hasOwnProperty.call(t,s)&&(i[s]=t[s]);i.originalType=e,i.mdxType="string"==typeof e?e:o,l[1]=i;for(var p=2;p<a;p++)l[p]=r[p];return n.createElement.apply(null,l)}return n.createElement.apply(null,r)}f.displayName="MDXCreateElement"},3:function(e,t,r){r.r(t),r.d(t,{frontMatter:function(){return i},contentTitle:function(){return s},metadata:function(){return p},assets:function(){return u},toc:function(){return c},default:function(){return m}});var n=r(7462),o=r(3366),a=(r(7294),r(3905)),l=["components"],i={title:"How to run a Deploy Cluster on Aws OpenShift setup with help of operator",tags:["cluster-operator"]},s=void 0,p={permalink:"/integration-server-gradle-plugin/blog/2021/12/10/run-deploy-openshift-cluster-with-operator",source:"@site/blog/2021-12-10-run-deploy-openshift-cluster-with-operator.md",title:"How to run a Deploy Cluster on Aws OpenShift setup with help of operator",description:"Requirements",date:"2021-12-10T00:00:00.000Z",formattedDate:"December 10, 2021",tags:[{label:"cluster-operator",permalink:"/integration-server-gradle-plugin/blog/tags/cluster-operator"}],readingTime:1.92,truncated:!1,authors:[],nextItem:{title:"How to run a Kube Scanning tests",permalink:"/integration-server-gradle-plugin/blog/2021/12/08/run-kube-scanning-test"}},u={authorsImageUrls:[]},c=[{value:"Requirements",id:"requirements",children:[]},{value:"Pre-requisites",id:"pre-requisites",children:[]},{value:"How the full flow works",id:"how-the-full-flow-works",children:[]},{value:"Example",id:"example",children:[]}],f={toc:c};function m(e){var t=e.components,r=(0,o.Z)(e,l);return(0,a.kt)("wrapper",(0,n.Z)({},f,r,{components:t,mdxType:"MDXLayout"}),(0,a.kt)("h2",{id:"requirements"},"Requirements"),(0,a.kt)("p",null,"Documentation is applicable for a version ",(0,a.kt)("strong",{parentName:"p"},"10.4.0-1209.942")," or later."),(0,a.kt)("h2",{id:"pre-requisites"},"Pre-requisites"),(0,a.kt)("p",null,"There are a couple of prerequisites which have to be performed in order to run the automation.\nYou have to:"),(0,a.kt)("ul",null,(0,a.kt)("li",{parentName:"ul"},(0,a.kt)("a",{parentName:"li",href:"https://docs.openshift.com/rosa/rosa_getting_started/rosa-creating-cluster.html"},"create a cluster itself on AWS OpenShift"),"."),(0,a.kt)("li",{parentName:"ul"},(0,a.kt)("a",{parentName:"li",href:"https://docs.openshift.com/container-platform/4.2/cli_reference/openshift_cli/getting-started-cli.html"},"install oc on your machine"),". "),(0,a.kt)("li",{parentName:"ul"},"add to your ",(0,a.kt)("inlineCode",{parentName:"li"},"~/.gradle/properties")," 2 values:")),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-shell",metastring:"script",script:!0},"ocLogin=...\nocPassword=... \n")),(0,a.kt)("ul",null,(0,a.kt)("li",{parentName:"ul"},(0,a.kt)("a",{parentName:"li",href:"https://docs.openshift.com/container-platform/4.2/storage/persistent_storage/persistent-storage-efs.html"},"create EFS class storage"))),(0,a.kt)("h2",{id:"how-the-full-flow-works"},"How the full flow works"),(0,a.kt)("ul",null,(0,a.kt)("li",{parentName:"ul"},"Installing a docker based Deploy instance, because we will use Deploy to create necessary resources in kubernetes and to deploy an operator."),(0,a.kt)("li",{parentName:"ul"},"Checking out ",(0,a.kt)("a",{parentName:"li",href:"https://github.com/xebialabs/xl-deploy-kubernetes-operator"},"Deploy operator")," and modifying the configuration based on the user input"),(0,a.kt)("li",{parentName:"ul"},"Installing ",(0,a.kt)("a",{parentName:"li",href:"https://docs.xebialabs.com/v.10.3/deploy/how-to/install-the-xl-cli/"},"XL CLI")," to apply YAML files "),(0,a.kt)("li",{parentName:"ul"},"Verifying that deployment was successful and all required resources were created in kubernetes. If something went wrong, you'll be notified about it in logs.")),(0,a.kt)("p",null,"You can also check this ",(0,a.kt)("a",{parentName:"p",href:"https://xebialabs.github.io/xl-deploy-kubernetes-operator/docs/manual/openshift"},"documentation")," for\nmore information."),(0,a.kt)("p",null,"All of this is automated and can be triggered by ",(0,a.kt)("inlineCode",{parentName:"p"},"./gradlew clean :core:startIntegrationServer -PoperatorOpenshiftItest=true --stacktrace"),"."),(0,a.kt)("p",null,"When you would like to stop your cluster you can run ",(0,a.kt)("inlineCode",{parentName:"p"},"./gradlew  :core:shutdownIntegrationServer -PoperatorOpenshiftItest=true --stacktrace"),".\nIt will undeploy all CIs, remove all deployed resources on kubernetes and clean all created PVC."),(0,a.kt)("h2",{id:"example"},"Example"),(0,a.kt)("p",null,"An example for a complete configuration:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-groovy"},'deployIntegrationServer {\n    cli {\n        overlays = [\n                ext: [\n                        fileTree(dir: "$rootDir/config/cli", includes: ["**/*.py"])\n                ],\n                lib: [\n                        "com.xebialabs.xl-platform.test-utils:py-modules:${testUtilsVersion}@jar"\n                ]\n        ]\n    }\n    cluster {\n        enable = true\n        profile = \'operator\'\n        publicPort = 10001\n    }\n    clusterProfiles {\n        operator {\n            activeProviderName = "aws-openshift"\n            awsOpenshift {\n                apiServerURL = \'https://api.yourhost.lnfl.p1.openshiftapps.com:6443\'\n                host = \'router-default.apps.yourhost.lnfl.p1.openshiftapps.com\'\n                name = \'aws-openshift-test-cluster\'\n                oauthHostName = "oauth-openshift.apps.yourhost.lnfl.p1.openshiftapps.com"\n                operatorImage = \'acierto/deploy-operator:1.0.6-openshift\'\n                operatorPackageVersion = "1.0.1"\n            }\n        }\n    }\n    servers {\n        server01 {\n            dockerImage = "xebialabsunsupported/xl-deploy"\n            pingRetrySleepTime = 10\n            pingTotalTries = 120\n            version = "${xlDeployTrialVersion}"\n        }\n        server02 {\n\n        }\n    }\n    workers {\n        worker01 {\n            dockerImage = "xebialabsunsupported/deploy-task-engine"\n        }\n        worker02 {\n        }\n    }\n}\n')),(0,a.kt)("p",null,"The cluster will be created with amount of servers and workers specified in the configuration. For this case,\nit will create 2 masters and 2 workers. The final URL to connect to UI is: ",(0,a.kt)("inlineCode",{parentName:"p"},"router-default.apps.yourhost.lnfl.p1.openshiftapps.com"),".\nIn case if you want to update the operator and use your own, you can change ",(0,a.kt)("inlineCode",{parentName:"p"},"operatorImage"),". As you can see from this\nexample, that's exactly what happened. ",(0,a.kt)("inlineCode",{parentName:"p"},"acierto/deploy-operator:1.0.6-openshift")," is not the official operator.\nInformation about ",(0,a.kt)("inlineCode",{parentName:"p"},"apiServerURL"),", ",(0,a.kt)("inlineCode",{parentName:"p"},"host")," and ",(0,a.kt)("inlineCode",{parentName:"p"},"oauthHostName")," you should check in your OpenShift Cluster console."))}m.isMDXComponent=!0}}]);