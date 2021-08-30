"use strict";(self.webpackChunkdocs=self.webpackChunkdocs||[]).push([[240],{3905:function(e,t,n){n.d(t,{Zo:function(){return d},kt:function(){return g}});var o=n(7294);function r(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function i(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var o=Object.getOwnPropertySymbols(e);t&&(o=o.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,o)}return n}function a(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?i(Object(n),!0).forEach((function(t){r(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):i(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function l(e,t){if(null==e)return{};var n,o,r=function(e,t){if(null==e)return{};var n,o,r={},i=Object.keys(e);for(o=0;o<i.length;o++)n=i[o],t.indexOf(n)>=0||(r[n]=e[n]);return r}(e,t);if(Object.getOwnPropertySymbols){var i=Object.getOwnPropertySymbols(e);for(o=0;o<i.length;o++)n=i[o],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(r[n]=e[n])}return r}var u=o.createContext({}),c=function(e){var t=o.useContext(u),n=t;return e&&(n="function"==typeof e?e(t):a(a({},t),e)),n},d=function(e){var t=c(e.components);return o.createElement(u.Provider,{value:t},e.children)},p={inlineCode:"code",wrapper:function(e){var t=e.children;return o.createElement(o.Fragment,{},t)}},s=o.forwardRef((function(e,t){var n=e.components,r=e.mdxType,i=e.originalType,u=e.parentName,d=l(e,["components","mdxType","originalType","parentName"]),s=c(n),g=r,m=s["".concat(u,".").concat(g)]||s[g]||p[g]||i;return n?o.createElement(m,a(a({ref:t},d),{},{components:n})):o.createElement(m,a({ref:t},d))}));function g(e,t){var n=arguments,r=t&&t.mdxType;if("string"==typeof e||r){var i=n.length,a=new Array(i);a[0]=s;var l={};for(var u in t)hasOwnProperty.call(t,u)&&(l[u]=t[u]);l.originalType=e,l.mdxType="string"==typeof e?e:r,a[1]=l;for(var c=2;c<i;c++)a[c]=n[c];return o.createElement.apply(null,a)}return o.createElement.apply(null,n)}s.displayName="MDXCreateElement"},8941:function(e,t,n){n.r(t),n.d(t,{frontMatter:function(){return l},contentTitle:function(){return u},metadata:function(){return c},toc:function(){return d},default:function(){return s}});var o=n(7462),r=n(3366),i=(n(7294),n(3905)),a=["components"],l={sidebar_position:9},u="Development",c={unversionedId:"getting-started/development",id:"getting-started/development",isDocsHomePage:!1,title:"Development",description:"How to build the plugin",source:"@site/docs/getting-started/development.md",sourceDirName:"getting-started",slug:"/getting-started/development",permalink:"/integration-server-gradle-plugin/docs/getting-started/development",version:"current",sidebarPosition:9,frontMatter:{sidebar_position:9},sidebar:"tutorialSidebar",previous:{title:"Limitations",permalink:"/integration-server-gradle-plugin/docs/getting-started/limitations"}},d=[{value:"How to build the plugin",id:"how-to-build-the-plugin",children:[]},{value:"Where documentation resides",id:"where-documentation-resides",children:[]},{value:"How to run documentation site locally",id:"how-to-run-documentation-site-locally",children:[]},{value:"How to generate the documentation for GitHub",id:"how-to-generate-the-documentation-for-github",children:[]},{value:"Troubleshooting",id:"troubleshooting",children:[]}],p={toc:d};function s(e){var t=e.components,n=(0,r.Z)(e,a);return(0,i.kt)("wrapper",(0,o.Z)({},p,n,{components:t,mdxType:"MDXLayout"}),(0,i.kt)("h1",{id:"development"},"Development"),(0,i.kt)("h2",{id:"how-to-build-the-plugin"},"How to build the plugin"),(0,i.kt)("p",null,(0,i.kt)("inlineCode",{parentName:"p"},"./gradlew clean build publishToMavenLocal snapshot")),(0,i.kt)("p",null,"This command will do a clean build and publish it as a snapshot version to your local maven repository.\nSo that in the project where you use the plugin you can just point to a snapshot to test your changes.  "),(0,i.kt)("p",null,"Example:"),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-groovy"},'buildscript {\n    repositories {\n        maven {\n            url "https://plugins.gradle.org/m2/"\n        }\n    }\n    dependencies {\n        classpath("com.xebialabs.gradle.plugins:integration-server-gradle-plugin:10.3.0-SNAPSHOT")\n    }\n}\n')),(0,i.kt)("h2",{id:"where-documentation-resides"},"Where documentation resides"),(0,i.kt)("p",null,"You can find the documentation to edit in documentation/docs folder. The ",(0,i.kt)("inlineCode",{parentName:"p"},"docs")," folder contains built documentation\nwhich is served on GitHub Pages."),(0,i.kt)("h2",{id:"how-to-run-documentation-site-locally"},"How to run documentation site locally"),(0,i.kt)("p",null,(0,i.kt)("inlineCode",{parentName:"p"},"./gradlew yarnRunStart")),(0,i.kt)("p",null,"The site will be opened automatically in your default browser on page: ",(0,i.kt)("a",{parentName:"p",href:"http://localhost:3000/integration-server-gradle-plugin/"},"http://localhost:3000/integration-server-gradle-plugin/")," "),(0,i.kt)("h2",{id:"how-to-generate-the-documentation-for-github"},"How to generate the documentation for GitHub"),(0,i.kt)("p",null,(0,i.kt)("inlineCode",{parentName:"p"},"./gradlew docBuild")," and commit all modified files in docs folder."),(0,i.kt)("h2",{id:"troubleshooting"},"Troubleshooting"),(0,i.kt)("p",null,"In case you have to debug the plugin in the application, you can add a parameter ",(0,i.kt)("inlineCode",{parentName:"p"},"-Dorg.gradle.debug=true"),"."),(0,i.kt)("p",null,"The full command can look like this:"),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-shell",metastring:"script",script:!0},"./gradlew clean startIntegrationServer --stacktrace -Dorg.gradle.debug=true --no-daemon\n")),(0,i.kt)("p",null,"Then in Intellij IDEA you are connecting to remote port 5005. The gradle task will proceed executing only after\nyou will be connected to this port. If you have some troubles with it, you might need first to execute: ",(0,i.kt)("inlineCode",{parentName:"p"},"./gradlew --stop")),(0,i.kt)("p",null,"When you run the job on CI pipeline, and the error doesn't give a clue what is going on, it's better to add ",(0,i.kt)("inlineCode",{parentName:"p"},"--stactrace"),"\nto get a better idea where exactly it fails."))}s.isMDXComponent=!0}}]);