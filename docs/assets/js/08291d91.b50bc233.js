"use strict";(self.webpackChunkdocs=self.webpackChunkdocs||[]).push([[6073],{3905:function(e,t,n){n.d(t,{Zo:function(){return s},kt:function(){return m}});var r=n(7294);function a(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function i(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);t&&(r=r.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,r)}return n}function o(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?i(Object(n),!0).forEach((function(t){a(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):i(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function l(e,t){if(null==e)return{};var n,r,a=function(e,t){if(null==e)return{};var n,r,a={},i=Object.keys(e);for(r=0;r<i.length;r++)n=i[r],t.indexOf(n)>=0||(a[n]=e[n]);return a}(e,t);if(Object.getOwnPropertySymbols){var i=Object.getOwnPropertySymbols(e);for(r=0;r<i.length;r++)n=i[r],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(a[n]=e[n])}return a}var c=r.createContext({}),u=function(e){var t=r.useContext(c),n=t;return e&&(n="function"==typeof e?e(t):o(o({},t),e)),n},s=function(e){var t=u(e.components);return r.createElement(c.Provider,{value:t},e.children)},p={inlineCode:"code",wrapper:function(e){var t=e.children;return r.createElement(r.Fragment,{},t)}},d=r.forwardRef((function(e,t){var n=e.components,a=e.mdxType,i=e.originalType,c=e.parentName,s=l(e,["components","mdxType","originalType","parentName"]),d=u(n),m=a,g=d["".concat(c,".").concat(m)]||d[m]||p[m]||i;return n?r.createElement(g,o(o({ref:t},s),{},{components:n})):r.createElement(g,o({ref:t},s))}));function m(e,t){var n=arguments,a=t&&t.mdxType;if("string"==typeof e||a){var i=n.length,o=new Array(i);o[0]=d;var l={};for(var c in t)hasOwnProperty.call(t,c)&&(l[c]=t[c]);l.originalType=e,l.mdxType="string"==typeof e?e:a,o[1]=l;for(var u=2;u<i;u++)o[u]=n[u];return r.createElement.apply(null,o)}return r.createElement.apply(null,n)}d.displayName="MDXCreateElement"},5103:function(e,t,n){n.r(t),n.d(t,{frontMatter:function(){return l},contentTitle:function(){return c},metadata:function(){return u},assets:function(){return s},toc:function(){return p},default:function(){return m}});var r=n(7462),a=n(3366),i=(n(7294),n(3905)),o=["components"],l={title:"How to run a Kube Scanning tests",tags:["kube-scanning-tests"]},c=void 0,u={permalink:"/integration-server-gradle-plugin/blog/2021/12/08/run-kube-scanning-test",source:"@site/blog/2021-12-08-run-kube-scanning-test.md",title:"How to run a Kube Scanning tests",description:"Introduction",date:"2021-12-08T00:00:00.000Z",formattedDate:"December 8, 2021",tags:[{label:"kube-scanning-tests",permalink:"/integration-server-gradle-plugin/blog/tags/kube-scanning-tests"}],readingTime:1.375,truncated:!1,authors:[],prevItem:{title:"How to run a Deploy Cluster on Aws OpenShift setup with help of operator",permalink:"/integration-server-gradle-plugin/blog/2021/12/10/run-deploy-openshift-cluster-with-operator"},nextItem:{title:"How to run a simple integration test",permalink:"/integration-server-gradle-plugin/blog/2021/09/02/run-integration-test"}},s={authorsImageUrls:[]},p=[{value:"Introduction",id:"introduction",children:[]},{value:"Build gradle configuration for kube scanner",id:"build-gradle-configuration-for-kube-scanner",children:[]},{value:"Under the hood",id:"under-the-hood",children:[{value:"How to scan the kubernetes cluster which is running on AWS (EKS)?",id:"how-to-scan-the-kubernetes-cluster-which-is-running-on-aws-eks",children:[]}]}],d={toc:p};function m(e){var t=e.components,l=(0,a.Z)(e,o);return(0,i.kt)("wrapper",(0,r.Z)({},d,l,{components:t,mdxType:"MDXLayout"}),(0,i.kt)("h3",{id:"introduction"},"Introduction"),(0,i.kt)("p",null,"Create your Kube scanning test, for your custom plugin in against running kubernetes cluster.\nHere we are using the ",(0,i.kt)("a",{parentName:"p",href:"https://github.com/aquasecurity/kube-bench/tree/0d1bd2bbd95608957be024c12d03a0510325e5e2"},"Kube-bench")," tool that checks the Kubernetes cluster is deployed securely by running the necessary checks documented in the CIS Kubernetes Benchmark."),(0,i.kt)("h3",{id:"build-gradle-configuration-for-kube-scanner"},"Build gradle configuration for kube scanner"),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-groovy"},'deployIntegrationServer {\n    kubeScanner {\n        awsRegion = \'eu-west-1\' \n        logOutput = true\n        kubeBenchTagVersion = "v0.6.5"\n        command = ["-v", "3", "logtostrerr"]\n    } \n}\n')),(0,i.kt)("table",null,(0,i.kt)("thead",{parentName:"table"},(0,i.kt)("tr",{parentName:"thead"},(0,i.kt)("th",{parentName:"tr",align:"center"},"Name"),(0,i.kt)("th",{parentName:"tr",align:"center"},"Type"),(0,i.kt)("th",{parentName:"tr",align:"center"},"Default Value"),(0,i.kt)("th",{parentName:"tr",align:"center"},"Description"))),(0,i.kt)("tbody",{parentName:"table"},(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"awsRegion"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"}),(0,i.kt)("td",{parentName:"tr",align:"center"},"By default it will read from config ","[~/.aws/config]"," file.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"logOutput"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"false"),(0,i.kt)("td",{parentName:"tr",align:"center"},"To Log the command and output executed while running the test.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"kubeBenchTagVersion"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},"latest"),(0,i.kt)("td",{parentName:"tr",align:"center"},"By default it will use the latest main branch.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"command"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Optional"),(0,i.kt)("td",{parentName:"tr",align:"center"},'["kube-bench", "run", "--targets", "node","--benchmark", "eks-1.0"]'),(0,i.kt)("td",{parentName:"tr",align:"center"},"List of ",(0,i.kt)("a",{parentName:"td",href:"https://github.com/aquasecurity/kube-bench/blob/main/docs/flags-and-commands.md"},"command")," for running the test.")))),(0,i.kt)("h2",{id:"under-the-hood"},"Under the hood"),(0,i.kt)("p",null,"Great, we now have the setup done. Let's figure out how it works."),(0,i.kt)("h3",{id:"how-to-scan-the-kubernetes-cluster-which-is-running-on-aws-eks"},"How to scan the kubernetes cluster which is running on AWS (EKS)?"),(0,i.kt)("ul",null,(0,i.kt)("li",{parentName:"ul"},"By Running the below command, we can scan the Kubernetes cluster which is configured as current-context in ~/.kube/config. ")),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-shell",metastring:"script",script:!0},"./gradlew clean kubeScanning\n")),(0,i.kt)("ul",null,(0,i.kt)("li",{parentName:"ul"},"Firstly, it will clone the kube-bench repo with a tag to build/kube-scanning/kube-bench folder. As in the below illustration: ")),(0,i.kt)("p",null,(0,i.kt)("img",{alt:"kube-bench-repo",src:n(9497).Z})),(0,i.kt)("ul",null,(0,i.kt)("li",{parentName:"ul"},"Next, execute the steps for ",(0,i.kt)("a",{parentName:"li",href:"https://github.com/aquasecurity/kube-bench/blob/main/docs/running.md#running-in-an-eks-cluster"},"AWS-EKS-Cluster"),(0,i.kt)("ul",{parentName:"li"},(0,i.kt)("li",{parentName:"ul"},"Create the repository in AWS ECR "),(0,i.kt)("li",{parentName:"ul"},"Take docker build of kube-bench with tag"),(0,i.kt)("li",{parentName:"ul"},"Push the created image to AWS ECR"),(0,i.kt)("li",{parentName:"ul"},"Update the job-eks.yaml with the latest image which we generate in a previous step and run the job.")))),(0,i.kt)("p",null,(0,i.kt)("img",{alt:"kube-bench-aws-eks-command",src:n(6793).Z})),(0,i.kt)("ul",null,(0,i.kt)("li",{parentName:"ul"},(0,i.kt)("p",{parentName:"li"},"Once the above command execution is completed, we can find the report in build/kube-scanning/report folder, like the below sample log."),(0,i.kt)("p",{parentName:"li"},(0,i.kt)("img",{alt:"kube-bench-aws-eks-report",src:n(4480).Z})))),(0,i.kt)("div",{className:"admonition admonition-info alert alert--info"},(0,i.kt)("div",{parentName:"div",className:"admonition-heading"},(0,i.kt)("h5",{parentName:"div"},(0,i.kt)("span",{parentName:"h5",className:"admonition-icon"},(0,i.kt)("svg",{parentName:"span",xmlns:"http://www.w3.org/2000/svg",width:"14",height:"16",viewBox:"0 0 14 16"},(0,i.kt)("path",{parentName:"svg",fillRule:"evenodd",d:"M7 2.3c3.14 0 5.7 2.56 5.7 5.7s-2.56 5.7-5.7 5.7A5.71 5.71 0 0 1 1.3 8c0-3.14 2.56-5.7 5.7-5.7zM7 1C3.14 1 0 4.14 0 8s3.14 7 7 7 7-3.14 7-7-3.14-7-7-7zm1 3H6v5h2V4zm0 6H6v2h2v-2z"}))),"info")),(0,i.kt)("div",{parentName:"div",className:"admonition-content"},(0,i.kt)("p",{parentName:"div"},(0,i.kt)("img",{alt:"note",src:n(8917).Z})))))}m.isMDXComponent=!0},6793:function(e,t,n){t.Z=n.p+"assets/images/kube-bench-aws-command-fff41d238a00d0d7b4c6df9a6ad69602.png"},4480:function(e,t,n){t.Z=n.p+"assets/images/kube-bench-aws-eks-report-5d522083a5f071bc33e31af5b835efda.png"},9497:function(e,t,n){t.Z=n.p+"assets/images/kube-bench-repo-1d5e8dedccdf9bad50032aff877c74f7.png"},8917:function(e,t,n){t.Z=n.p+"assets/images/worker-node-note-fddb274e34872363e56e4c1c855df58e.png"}}]);