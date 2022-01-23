"use strict";(self.webpackChunkdocs=self.webpackChunkdocs||[]).push([[3404],{3905:function(e,t,r){r.d(t,{Zo:function(){return s},kt:function(){return k}});var a=r(7294);function n(e,t,r){return t in e?Object.defineProperty(e,t,{value:r,enumerable:!0,configurable:!0,writable:!0}):e[t]=r,e}function i(e,t){var r=Object.keys(e);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);t&&(a=a.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),r.push.apply(r,a)}return r}function o(e){for(var t=1;t<arguments.length;t++){var r=null!=arguments[t]?arguments[t]:{};t%2?i(Object(r),!0).forEach((function(t){n(e,t,r[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(r)):i(Object(r)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(r,t))}))}return e}function l(e,t){if(null==e)return{};var r,a,n=function(e,t){if(null==e)return{};var r,a,n={},i=Object.keys(e);for(a=0;a<i.length;a++)r=i[a],t.indexOf(r)>=0||(n[r]=e[r]);return n}(e,t);if(Object.getOwnPropertySymbols){var i=Object.getOwnPropertySymbols(e);for(a=0;a<i.length;a++)r=i[a],t.indexOf(r)>=0||Object.prototype.propertyIsEnumerable.call(e,r)&&(n[r]=e[r])}return n}var p=a.createContext({}),c=function(e){var t=a.useContext(p),r=t;return e&&(r="function"==typeof e?e(t):o(o({},t),e)),r},s=function(e){var t=c(e.components);return a.createElement(p.Provider,{value:t},e.children)},d={inlineCode:"code",wrapper:function(e){var t=e.children;return a.createElement(a.Fragment,{},t)}},m=a.forwardRef((function(e,t){var r=e.components,n=e.mdxType,i=e.originalType,p=e.parentName,s=l(e,["components","mdxType","originalType","parentName"]),m=c(r),k=n,g=m["".concat(p,".").concat(k)]||m[k]||d[k]||i;return r?a.createElement(g,o(o({ref:t},s),{},{components:r})):a.createElement(g,o({ref:t},s))}));function k(e,t){var r=arguments,n=t&&t.mdxType;if("string"==typeof e||n){var i=r.length,o=new Array(i);o[0]=m;var l={};for(var p in t)hasOwnProperty.call(t,p)&&(l[p]=t[p]);l.originalType=e,l.mdxType="string"==typeof e?e:n,o[1]=l;for(var c=2;c<i;c++)o[c]=r[c];return a.createElement.apply(null,o)}return a.createElement.apply(null,r)}m.displayName="MDXCreateElement"},7533:function(e,t,r){r.r(t),r.d(t,{frontMatter:function(){return l},contentTitle:function(){return p},metadata:function(){return c},toc:function(){return s},default:function(){return m}});var a=r(7462),n=r(3366),i=(r(7294),r(3905)),o=["components"],l={sidebar_position:1},p="Configuration",c={unversionedId:"release/configuration",id:"release/configuration",isDocsHomePage:!1,title:"Configuration",description:"The first section level",source:"@site/docs/release/configuration.md",sourceDirName:"release",slug:"/release/configuration",permalink:"/integration-server-gradle-plugin/docs/release/configuration",tags:[],version:"current",sidebarPosition:1,frontMatter:{sidebar_position:1},sidebar:"tutorialSidebar",previous:{title:"Development",permalink:"/integration-server-gradle-plugin/docs/deploy/development"},next:{title:"Examples",permalink:"/integration-server-gradle-plugin/docs/release/examples"}},s=[{value:"The first section level",id:"the-first-section-level",children:[]},{value:"Cluster section",id:"cluster-section",children:[]},{value:"Cluster profiles for operator",id:"cluster-profiles-for-operator",children:[]},{value:"Servers section",id:"servers-section",children:[{value:"AWS Openshift profile",id:"aws-openshift-profile",children:[]},{value:"Azure AKS profile",id:"azure-aks-profile",children:[]},{value:"GCP GKE profile",id:"gcp-gke-profile",children:[]},{value:"Onprem Minikube profile",id:"onprem-minikube-profile",children:[]},{value:"AWS EKS profile",id:"aws-eks-profile",children:[]},{value:"Operator server section",id:"operator-server-section",children:[]}]}],d={toc:s};function m(e){var t=e.components,l=(0,n.Z)(e,o);return(0,i.kt)("wrapper",(0,a.Z)({},d,l,{components:t,mdxType:"MDXLayout"}),(0,i.kt)("h1",{id:"configuration"},"Configuration"),(0,i.kt)("h2",{id:"the-first-section-level"},"The first section level"),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-groovy",metastring:"title=build.gradle",title:"build.gradle"},"releaseIntegrationServer {\n    cluster {}\n    clusterProfiles {}\n    servers {}\n}\n")),(0,i.kt)("table",null,(0,i.kt)("thead",{parentName:"table"},(0,i.kt)("tr",{parentName:"thead"},(0,i.kt)("th",{parentName:"tr",align:"center"},"Name"),(0,i.kt)("th",{parentName:"tr",align:"center"},"Description"))),(0,i.kt)("tbody",{parentName:"table"},(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"cluster"),(0,i.kt)("td",{parentName:"tr",align:"center"},"The configuration section for cluster based setup. By default it's disabled.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"clusterProfiles"),(0,i.kt)("td",{parentName:"tr",align:"center"},"In this section you can define multiple profiles for different providers and in cluster section define which profile is active now.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"operatorServer"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Operator is installed/upgraded with help of Deploy server. It can be configured of different image/version that one is running on cluster.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"servers"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Server configurations, currently, it's possible to configure only one.")))),(0,i.kt)("h2",{id:"cluster-section"},"Cluster section"),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-groovy",metastring:"title=build.gradle",title:"build.gradle"},"releaseIntegrationServer {\n    cluster {\n        debugSuspend = true\n        enable = true\n        enableDebug = true\n        profile = 'operator'\n        publicPort = 1000\n    }\n}\n")),(0,i.kt)("table",null,(0,i.kt)("thead",{parentName:"table"},(0,i.kt)("tr",{parentName:"thead"},(0,i.kt)("th",{parentName:"tr",align:"center"},"Name"),(0,i.kt)("th",{parentName:"tr",align:"center"},"Type"),(0,i.kt)("th",{parentName:"tr",align:"center"},"Default Value"),(0,i.kt)("th",{parentName:"tr",align:"center"},"Description"))),(0,i.kt)("tbody",{parentName:"table"},(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"debugSuspend"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"false"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Suspend the start of the process before the remoting tool is attached. Take in mind that you have to attach to all processes to be able to completely run the cluster.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"enable"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"false"),(0,i.kt)("td",{parentName:"tr",align:"center"},"If true, cluster setup will be enabled.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"enableDebug"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"false"),(0,i.kt)("td",{parentName:"tr",align:"center"},"If true, debug will be enabled on all masters and workers. The exposed ports to connect will be randomly defined. You can check with ",(0,i.kt)("inlineCode",{parentName:"td"},"docker ps")," which port was exposed for debugging.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"profile"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"operator"),(0,i.kt)("td",{parentName:"tr",align:"center"},"The way to run the setup. For now only 1 option is available - 'operator'.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"publicPort"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"8080"),(0,i.kt)("td",{parentName:"tr",align:"center"},"The port to connect to the cluster.")))),(0,i.kt)("p",null,"Example where to check for debugging ports to attach:"),(0,i.kt)("p",null,(0,i.kt)("img",{alt:"Example",src:r(6262).Z})),(0,i.kt)("p",null,"Example for operator configuration:"),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-shell",metastring:"script",script:!0},"    ...\n    cluster {\n        enable = true\n        profile = 'operator'\n        publicPort = 10001\n    }\n    clusterProfiles {\n        operator {\n            activeProviderName = \"aws-openshift\"\n            awsOpenshift {\n                apiServerURL = 'https://yourhost.openshiftapps.com:6443'\n                host = 'router-default.yourhost.openshiftapps.com'\n                name = 'aws-openshift-test-cluster'\n                oauthHostName = \"oauth-openshift.yourhost.openshiftapps.com\"\n                operatorImage = 'acierto/release-operator:1.0.6-openshift'\n                operatorPackageVersion = \"1.0.7\"\n            }\n        }\n    }\n    ...\n")),(0,i.kt)("h2",{id:"cluster-profiles-for-operator"},"Cluster profiles for operator"),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-shell",metastring:"script",script:!0},"clusterProfiles {\n    operator {\n        activeProviderName = \"aws-openshift\"\n        awsOpenshift {\n            apiServerURL = 'https://api.acierto.lnfl.p1.openshiftapps.com:6443'\n            host = 'router-default.apps.acierto.lnfl.p1.openshiftapps.com'\n            name = 'aws-openshift-test-cluster'\n            oauthHostName = \"oauth-openshift.apps.acierto.lnfl.p1.openshiftapps.com\"\n            operatorImage = 'acierto/release-operator:1.0.6-openshift'\n            operatorPackageVersion = \"1.0.1\"\n        }\n        azureAks {\n            clusterNodeCount = 3\n            clusterNodeVmSize = 'Standard_DS2_v2'\n            kubernetesVersion = '1.20'\n            location = 'northcentralus'\n            name = 'azure-aks-test-cluster'\n            skipExisting = false\n            azUsername = 'azure_username'\n            azPassword = 'secret'\n        }\n        gcpGke {\n            accountCredFile = 'path_to_the_cred_json_file'\n            accountName = 'gcp-gke-usert@apollo-playground.iam.gserviceaccount.com'\n            clusterNodeCount = 3\n            clusterNodeVmSize = 'e2-standard-2'\n            kubernetesVersion = '1.20.11-gke.1801'\n            name = 'gcp-gke-test-cluster'\n            projectName = 'apollo-playground'\n            regionZone = 'us-central1-a'\n            skipExisting = false\n        }        \n        onPremise {\n            name = 'onprem-test-cluster'\n            clusterNodeCpus = 4\n            clusterNodeMemory = 15000\n            kubernetesVersion = '1.20.0'\n            skipExisting = false\n        }\n        awsEks {\n            region = \"us-east-1\"\n            stack = \"release-operator-test\"\n            clusterName = \"release-operator-cluster-test\"\n            nodeGroupName = \"release-operator-cluster-nodegroup\"\n            clusterNodeCount = 2\n            sshKeyName = \"release-operator-ssh-key\"\n            fileSystemName = \"release-operator-efs-test\"\n            kubernetesVersion = \"1.20\"\n            skipExisting = true\n            stackTimeoutSeconds = 1500000\n            stackSleepTimeBeforeRetrySeconds = 300000\n            route53InsycAwaitTimeoutSeconds = 300000\n            accessKey = \"AWS access key\"\n            secretKey = \"AWS Secret key\"\n        }\n    }\n}\n")),(0,i.kt)("h2",{id:"servers-section"},"Servers section"),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-groovy",metastring:"title=build.gradle",title:"build.gradle"},"releaseIntegrationServer {\n   servers {\n       controlPlane { // The name of the section, you can name it as you wish\n           dockerImage = \"xebialabs/xl-release\" \n           httpPort = 5516\n           pingRetrySleepTime = 5\n           pingTotalTries = 120\n           version = '10.2.2'\n       }       \n   }   \n}\n")),(0,i.kt)("table",null,(0,i.kt)("thead",{parentName:"table"},(0,i.kt)("tr",{parentName:"thead"},(0,i.kt)("th",{parentName:"tr",align:"center"},"Name"),(0,i.kt)("th",{parentName:"tr",align:"center"},"Type"),(0,i.kt)("th",{parentName:"tr",align:"center"},"Default Value"),(0,i.kt)("th",{parentName:"tr",align:"center"},"Description"))),(0,i.kt)("tbody",{parentName:"table"},(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"dockerImage"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"None"),(0,i.kt)("td",{parentName:"tr",align:"center"},"When this property is specified, docker based setup will be performed. The name of the docker image, without version. Version is specified in the separate field or dedicated from gradle properties.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"httpPort"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Random port"),(0,i.kt)("td",{parentName:"tr",align:"center"},"The HTTP port for Release server.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"pingRetrySleepTime"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"10"),(0,i.kt)("td",{parentName:"tr",align:"center"},"During the startup of the server we check when it's completely booted. This property configures how long to sleep (in seconds) between retries.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"pingTotalTries"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"60"),(0,i.kt)("td",{parentName:"tr",align:"center"},"During the startup of the server we check when it's completely booted. This property configures how many times to retry.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"version"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"None"),(0,i.kt)("td",{parentName:"tr",align:"center"},"It can be specified in several ways. Or as a gradle property ",(0,i.kt)("inlineCode",{parentName:"td"},"xlReleaseVersion"),", via parameter or in ",(0,i.kt)("inlineCode",{parentName:"td"},"gradle.properties")," file or explicitly via this field.")))),(0,i.kt)("h3",{id:"aws-openshift-profile"},"AWS Openshift profile"),(0,i.kt)("p",null,(0,i.kt)("inlineCode",{parentName:"p"},'activeProviderName = "aws-openshift"')),(0,i.kt)("table",null,(0,i.kt)("thead",{parentName:"table"},(0,i.kt)("tr",{parentName:"thead"},(0,i.kt)("th",{parentName:"tr",align:"center"},"Name"),(0,i.kt)("th",{parentName:"tr",align:"center"},"Type"),(0,i.kt)("th",{parentName:"tr",align:"center"},"Default Value"),(0,i.kt)("th",{parentName:"tr",align:"center"},"Description"))),(0,i.kt)("tbody",{parentName:"table"},(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"apiServerURL"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Mandatory"),(0,i.kt)("td",{parentName:"tr",align:"center"},"-"),(0,i.kt)("td",{parentName:"tr",align:"center"},"The URL to your OpenShift cluster server API")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"host"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Mandatory"),(0,i.kt)("td",{parentName:"tr",align:"center"},"-"),(0,i.kt)("td",{parentName:"tr",align:"center"},"The public host on which cluster will be available to interact with. Basically it is your OpenShift router URL.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"keystorePassphrase"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"test123"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Keystore password to encrypt sensitive information in CIs")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"name"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Mandatory"),(0,i.kt)("td",{parentName:"tr",align:"center"},"-"),(0,i.kt)("td",{parentName:"tr",align:"center"},"The name of your cluster.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"operatorImage"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"xebialabs/release-operator:1.2.0-openshift"),(0,i.kt)("td",{parentName:"tr",align:"center"},"The image of operator which is going to be used to install the Reploy cluster")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"operatorPackageVersion"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"1.0.0"),(0,i.kt)("td",{parentName:"tr",align:"center"},"We deploy operator with help of Release, this is a version which will be used as a application package version.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"oauthHostName"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Mandatory"),(0,i.kt)("td",{parentName:"tr",align:"center"},"-"),(0,i.kt)("td",{parentName:"tr",align:"center"},"OAuth host name of your OpenShift cluster. It is used to get a new token based on your credentials. This token is required to interact with OpenShift cluster.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"repositoryKeystore"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Provided"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Keystore to encrypt sensitive information in CIs")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"storageClass"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"aws-efs"),(0,i.kt)("td",{parentName:"tr",align:"center"},"You can use another storage class, but you have to be sure that it is NFS based, otherwise it won't work.")))),(0,i.kt)("h3",{id:"azure-aks-profile"},"Azure AKS profile"),(0,i.kt)("p",null,(0,i.kt)("inlineCode",{parentName:"p"},'activeProviderName = "azure-aks"')),(0,i.kt)("table",null,(0,i.kt)("thead",{parentName:"table"},(0,i.kt)("tr",{parentName:"thead"},(0,i.kt)("th",{parentName:"tr",align:"center"},"Name"),(0,i.kt)("th",{parentName:"tr",align:"center"},"Type"),(0,i.kt)("th",{parentName:"tr",align:"center"},"Default Value"),(0,i.kt)("th",{parentName:"tr",align:"center"},"Description"))),(0,i.kt)("tbody",{parentName:"table"},(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"azUsername"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Mandatory"),(0,i.kt)("td",{parentName:"tr",align:"center"},"-"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Azure username to be used with az cli tool.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"azPassword"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Mandatory"),(0,i.kt)("td",{parentName:"tr",align:"center"},"-"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Azure password to be used with az cli tool.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"clusterNodeCount"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"2"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Number of the nodes that will be created during cluster creation on Azure.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"clusterNodeVmSize"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Standard_DS2_v2 (Azure default value)"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Node VM size named on Azure.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"kubernetesVersion"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Default Azure version is 1.20"),(0,i.kt)("td",{parentName:"tr",align:"center"},"The kubernetes version that will be custom string for each provider.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"location"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Mandatory"),(0,i.kt)("td",{parentName:"tr",align:"center"},"-"),(0,i.kt)("td",{parentName:"tr",align:"center"},"The Azure location that represents geo location where cluster will be running.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"name"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Mandatory"),(0,i.kt)("td",{parentName:"tr",align:"center"},"-"),(0,i.kt)("td",{parentName:"tr",align:"center"},"The name of your cluster.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"operatorImage"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"xebialabs/release-operator:1.2.0"),(0,i.kt)("td",{parentName:"tr",align:"center"},"The image of operator which is going to be used to install the Release cluster")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"operatorPackageVersion"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"1.2.0"),(0,i.kt)("td",{parentName:"tr",align:"center"},"We deploy operator with help of Deploy, this is a version which will be used as a application package version.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"skipExisting"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"true"),(0,i.kt)("td",{parentName:"tr",align:"center"},"For some cluster resources there are checks if resources exist, if set to true skip creation.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"storageClass"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"-"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Storage class prefix. On Azure with prefix are created new classes for file and disk storage.")))),(0,i.kt)("h3",{id:"gcp-gke-profile"},"GCP GKE profile"),(0,i.kt)("p",null,(0,i.kt)("inlineCode",{parentName:"p"},'activeProviderName = "gcp-gke"')),(0,i.kt)("table",null,(0,i.kt)("thead",{parentName:"table"},(0,i.kt)("tr",{parentName:"thead"},(0,i.kt)("th",{parentName:"tr",align:"center"},"Name"),(0,i.kt)("th",{parentName:"tr",align:"center"},"Type"),(0,i.kt)("th",{parentName:"tr",align:"center"},"Default Value"),(0,i.kt)("th",{parentName:"tr",align:"center"},"Description"))),(0,i.kt)("tbody",{parentName:"table"},(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"accountCredFile"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"-"),(0,i.kt)("td",{parentName:"tr",align:"center"},"A file path to read the access token credentials file.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"accountName"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Mandatory"),(0,i.kt)("td",{parentName:"tr",align:"center"},"-"),(0,i.kt)("td",{parentName:"tr",align:"center"},"GCP user account that will be used with ",(0,i.kt)("inlineCode",{parentName:"td"},"gcloud")," plugin.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"clusterNodeCount"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"3"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Number of the nodes that will be created during cluster creation on GCP.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"clusterNodeVmSize"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Medium_DS2_v2 (GCP default value)"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Node VM size named on GCP.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"kubernetesVersion"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"1.20.11-gke.1801"),(0,i.kt)("td",{parentName:"tr",align:"center"},"The kubernetes version that will be custom string for each provider.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"name"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Mandatory"),(0,i.kt)("td",{parentName:"tr",align:"center"},"-"),(0,i.kt)("td",{parentName:"tr",align:"center"},"The name of your cluster.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"operatorImage"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"xebialabs/release-operator:1.2.0"),(0,i.kt)("td",{parentName:"tr",align:"center"},"The image of operator which is going to be used to install the Release cluster")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"operatorPackageVersion"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"1.2.0"),(0,i.kt)("td",{parentName:"tr",align:"center"},"We deploy operator with help of Deploy, this is a version which will be used as a application package version.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"projectName"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Mandatory"),(0,i.kt)("td",{parentName:"tr",align:"center"},"-"),(0,i.kt)("td",{parentName:"tr",align:"center"},"The GCP project in which GKE cluster will be created.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"regionZone"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Mandatory"),(0,i.kt)("td",{parentName:"tr",align:"center"},"-"),(0,i.kt)("td",{parentName:"tr",align:"center"},"The cluster GEO zone where cluster instances will be located.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"skipExisting"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"true"),(0,i.kt)("td",{parentName:"tr",align:"center"},"For some cluster resources there are checks if resources exist, if set to true skip creation.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"storageClass"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"standard"),(0,i.kt)("td",{parentName:"tr",align:"center"},"You can use another storage class, but you have to be sure that it is NFS based, otherwise it won't work.")))),(0,i.kt)("h3",{id:"onprem-minikube-profile"},"Onprem Minikube profile"),(0,i.kt)("p",null,(0,i.kt)("inlineCode",{parentName:"p"},'activeProviderName = "onprem"')),(0,i.kt)("table",null,(0,i.kt)("thead",{parentName:"table"},(0,i.kt)("tr",{parentName:"thead"},(0,i.kt)("th",{parentName:"tr",align:"center"},"Name"),(0,i.kt)("th",{parentName:"tr",align:"center"},"Type"),(0,i.kt)("th",{parentName:"tr",align:"center"},"Default Value"),(0,i.kt)("th",{parentName:"tr",align:"center"},"Description"))),(0,i.kt)("tbody",{parentName:"table"},(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"clusterNodeCpus"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"-"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Number of CPUs that will be used by cluster.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"clusterNodeMemory"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"-"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Memory size in MB that will be used by cluster.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"kubernetesVersion"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"1.20.0"),(0,i.kt)("td",{parentName:"tr",align:"center"},"The kubernetes version that will be custom string for each provider.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"name"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Mandatory"),(0,i.kt)("td",{parentName:"tr",align:"center"},"-"),(0,i.kt)("td",{parentName:"tr",align:"center"},"The name of your cluster.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"operatorImage"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"xebialabs/release-operator:1.2.0"),(0,i.kt)("td",{parentName:"tr",align:"center"},"The image of operator which is going to be used to install the Release cluster")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"operatorPackageVersion"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"1.2.0"),(0,i.kt)("td",{parentName:"tr",align:"center"},"We deploy operator with help of Deploy, this is a version which will be used as a application package version.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"skipExisting"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"true"),(0,i.kt)("td",{parentName:"tr",align:"center"},"For some cluster resources there are checks if resources exist, if set to true skip creation.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"storageClass"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"standard"),(0,i.kt)("td",{parentName:"tr",align:"center"},"You can use another storage class, but you have to be sure that it is NFS based, otherwise it won't work.")))),(0,i.kt)("h3",{id:"aws-eks-profile"},"AWS EKS profile"),(0,i.kt)("p",null,(0,i.kt)("inlineCode",{parentName:"p"},'activeProviderName = "aws-eks"')),(0,i.kt)("table",null,(0,i.kt)("thead",{parentName:"table"},(0,i.kt)("tr",{parentName:"thead"},(0,i.kt)("th",{parentName:"tr",align:"center"},"Name"),(0,i.kt)("th",{parentName:"tr",align:"center"},"Type"),(0,i.kt)("th",{parentName:"tr",align:"center"},"Default Value"),(0,i.kt)("th",{parentName:"tr",align:"center"},"Description"))),(0,i.kt)("tbody",{parentName:"table"},(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"accessKey"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"-"),(0,i.kt)("td",{parentName:"tr",align:"center"},"AWS AccessKey to access aws cli tool.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"secretKey"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"-"),(0,i.kt)("td",{parentName:"tr",align:"center"},"AWS SecretKey to access aws cli tool.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"region"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"'us-east-1'"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Region where the cluster to be created.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"stack"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"'release-operator-test'"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Name of the AWS stack.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"clusterName"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"'release-operator-cluster-test'"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Name of the AWS EKS Cluster.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"nodeGroupName"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"'release-operator-cluster-nodegroup'"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Name of the nodeGroup. At present only two node groups are support.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"clusterNodeCount"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"2"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Number of the worker nodes to be created within node group, max node count of each group is 8.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"kubernetesVersion"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Default version is 1.20"),(0,i.kt)("td",{parentName:"tr",align:"center"},"The kubernetes version that will be custom string for each provider.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"sshKeyName"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"'release-operator-ssh-key'"),(0,i.kt)("td",{parentName:"tr",align:"center"},"ssh key for accessing Amazon EC2 instance.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"fileSystemName"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"'release-operator-efs-test'"),(0,i.kt)("td",{parentName:"tr",align:"center"},"AWS EFS file system name.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"operatorImage"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"xebialabs/release-operator:1.2.0"),(0,i.kt)("td",{parentName:"tr",align:"center"},"The image of operator which is going to be used to install the Release cluster")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"operatorPackageVersion"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"1.2.0"),(0,i.kt)("td",{parentName:"tr",align:"center"},"We deploy operator with help of Deploy, this is a version which will be used as a application package version.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"skipExisting"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"true"),(0,i.kt)("td",{parentName:"tr",align:"center"},"For some cluster resources there are checks if resources exist, if set to true skip creation.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"stackTimeoutSeconds"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"1500000"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Maximum wait time for 'Stack Creation' or 'Stack Deletion' in seconds.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"stackSleepTimeBeforeRetrySeconds"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"300000"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Polling period in seconds for 'Stack Creation' or 'Stack Deletion'.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"route53InsycAwaitTimeoutSeconds"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"300000"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Polling period in seconds for route53 provisioning.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"storageClass"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"\"aws-efs && gp2'"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Use gp2 storageclass for postgres and rabbitmq and  use 'aws-efs' storageclass for xl-release pods")))),(0,i.kt)("h3",{id:"operator-server-section"},"Operator server section"),(0,i.kt)("table",null,(0,i.kt)("thead",{parentName:"table"},(0,i.kt)("tr",{parentName:"thead"},(0,i.kt)("th",{parentName:"tr",align:"center"},"Name"),(0,i.kt)("th",{parentName:"tr",align:"center"},"Type"),(0,i.kt)("th",{parentName:"tr",align:"center"},"Default Value"),(0,i.kt)("th",{parentName:"tr",align:"center"},"Description"))),(0,i.kt)("tbody",{parentName:"table"},(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"dockerImage"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Required"),(0,i.kt)("td",{parentName:"tr",align:"center"},"None"),(0,i.kt)("td",{parentName:"tr",align:"center"},"It has to be specified explicitly. Example: xebialabs/xl-deploy")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"httpPort"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Random port"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Public http port for Deploy server. Can be useful in case of troubleshooting of the failed deployment of operator.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"pingRetrySleepTime"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"10"),(0,i.kt)("td",{parentName:"tr",align:"center"},"During the startup of the server we check when it's completely booted. This property configures how long to sleep (in seconds) between retries.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"pingTotalTries"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"60"),(0,i.kt)("td",{parentName:"tr",align:"center"},"During the startup of the server we check when it's completely booted. This property configures how many times to retry.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"version"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Required"),(0,i.kt)("td",{parentName:"tr",align:"center"},"None"),(0,i.kt)("td",{parentName:"tr",align:"center"},"It has to be specified explicitly.")))))}m.isMDXComponent=!0},6262:function(e,t,r){t.Z=r.p+"assets/images/cluster-debug-docker-ps-6acefdcdd5c6dd0f91ef89ef6914c86c.png"}}]);