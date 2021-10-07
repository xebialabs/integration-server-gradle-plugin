"use strict";(self.webpackChunkdocs=self.webpackChunkdocs||[]).push([[48],{3905:function(e,t,n){n.d(t,{Zo:function(){return c},kt:function(){return g}});var r=n(7294);function i(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function a(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);t&&(r=r.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,r)}return n}function o(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?a(Object(n),!0).forEach((function(t){i(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):a(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function l(e,t){if(null==e)return{};var n,r,i=function(e,t){if(null==e)return{};var n,r,i={},a=Object.keys(e);for(r=0;r<a.length;r++)n=a[r],t.indexOf(n)>=0||(i[n]=e[n]);return i}(e,t);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);for(r=0;r<a.length;r++)n=a[r],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(i[n]=e[n])}return i}var s=r.createContext({}),u=function(e){var t=r.useContext(s),n=t;return e&&(n="function"==typeof e?e(t):o(o({},t),e)),n},c=function(e){var t=u(e.components);return r.createElement(s.Provider,{value:t},e.children)},p={inlineCode:"code",wrapper:function(e){var t=e.children;return r.createElement(r.Fragment,{},t)}},d=r.forwardRef((function(e,t){var n=e.components,i=e.mdxType,a=e.originalType,s=e.parentName,c=l(e,["components","mdxType","originalType","parentName"]),d=u(n),g=i,m=d["".concat(s,".").concat(g)]||d[g]||p[g]||a;return n?r.createElement(m,o(o({ref:t},c),{},{components:n})):r.createElement(m,o({ref:t},c))}));function g(e,t){var n=arguments,i=t&&t.mdxType;if("string"==typeof e||i){var a=n.length,o=new Array(a);o[0]=d;var l={};for(var s in t)hasOwnProperty.call(t,s)&&(l[s]=t[s]);l.originalType=e,l.mdxType="string"==typeof e?e:i,o[1]=l;for(var u=2;u<a;u++)o[u]=n[u];return r.createElement.apply(null,o)}return r.createElement.apply(null,n)}d.displayName="MDXCreateElement"},8271:function(e,t,n){n.r(t),n.d(t,{frontMatter:function(){return l},contentTitle:function(){return s},metadata:function(){return u},toc:function(){return c},default:function(){return d}});var r=n(7462),i=n(3366),a=(n(7294),n(3905)),o=["components"],l={sidebar_position:1},s="Installation",u={unversionedId:"getting-started/installation",id:"version-10.3.0/getting-started/installation",isDocsHomePage:!1,title:"Installation",description:"Requirements",source:"@site/versioned_docs/version-10.3.0/getting-started/installation.md",sourceDirName:"getting-started",slug:"/getting-started/installation",permalink:"/integration-server-gradle-plugin/docs/10.3.0/getting-started/installation",tags:[],version:"10.3.0",sidebarPosition:1,frontMatter:{sidebar_position:1},sidebar:"version-10.4.0/tutorialSidebar",previous:{title:"Introduction",permalink:"/integration-server-gradle-plugin/docs/10.3.0/intro"},next:{title:"Plugin structure",permalink:"/integration-server-gradle-plugin/docs/10.3.0/getting-started/structure"}},c=[{value:"Requirements",id:"requirements",children:[]},{value:"Add the plugin",id:"add-the-plugin",children:[]},{value:"Running the integration server",id:"running-the-integration-server",children:[]}],p={toc:c};function d(e){var t=e.components,n=(0,i.Z)(e,o);return(0,a.kt)("wrapper",(0,r.Z)({},p,n,{components:t,mdxType:"MDXLayout"}),(0,a.kt)("h1",{id:"installation"},"Installation"),(0,a.kt)("h2",{id:"requirements"},"Requirements"),(0,a.kt)("p",null,"Integration Server based on Gradle and docker images. Therefore, you have to have on your machine pre-installed:"),(0,a.kt)("ul",null,(0,a.kt)("li",{parentName:"ul"},"JDK 11"),(0,a.kt)("li",{parentName:"ul"},"Docker"),(0,a.kt)("li",{parentName:"ul"},"Docker Compose  "),(0,a.kt)("li",{parentName:"ul"},"Gradle 6+")),(0,a.kt)("h2",{id:"add-the-plugin"},"Add the plugin"),(0,a.kt)("p",null,"In the root file ",(0,a.kt)("strong",{parentName:"p"},"build.gradle")," of your project define a plugin dependency like this:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-groovy"},'\nbuildscript {\n    repositories {\n        mavenCentral()\n        mavenLocal() // Optional, only required if you\'ll develop changes to the plugin.\n    }\n\n    dependencies {\n        classpath "com.xebialabs.gradle.plugins:integration-server-gradle-plugin:10.3.0-820.1249"\n    }\n}\n\napply plugin: \'integration.server\'\n\nintegrationServer {\n    servers {\n        controlPlane {\n            dockerImage = "xebialabs/xl-deploy" // docker hub repository\n            version = "10.2.2" // Here you can point to a version you\'d like to run\n        }\n    }\n}\n')),(0,a.kt)("div",{className:"admonition admonition-tip alert alert--success"},(0,a.kt)("div",{parentName:"div",className:"admonition-heading"},(0,a.kt)("h5",{parentName:"div"},(0,a.kt)("span",{parentName:"h5",className:"admonition-icon"},(0,a.kt)("svg",{parentName:"span",xmlns:"http://www.w3.org/2000/svg",width:"12",height:"16",viewBox:"0 0 12 16"},(0,a.kt)("path",{parentName:"svg",fillRule:"evenodd",d:"M6.5 0C3.48 0 1 2.19 1 5c0 .92.55 2.25 1 3 1.34 2.25 1.78 2.78 2 4v1h5v-1c.22-1.22.66-1.75 2-4 .45-.75 1-2.08 1-3 0-2.81-2.48-5-5.5-5zm3.64 7.48c-.25.44-.47.8-.67 1.11-.86 1.41-1.25 2.06-1.45 3.23-.02.05-.02.11-.02.17H5c0-.06 0-.13-.02-.17-.2-1.17-.59-1.83-1.45-3.23-.2-.31-.42-.67-.67-1.11C2.44 6.78 2 5.65 2 5c0-2.2 2.02-4 4.5-4 1.22 0 2.36.42 3.22 1.19C10.55 2.94 11 3.94 11 5c0 .66-.44 1.78-.86 2.48zM4 14h5c-.23 1.14-1.3 2-2.5 2s-2.27-.86-2.5-2z"}))),"tip")),(0,a.kt)("div",{parentName:"div",className:"admonition-content"},(0,a.kt)("p",{parentName:"div"},"This plugin version works only with Deploy 10.2.x and 10.3.x, you have to match the plugin version with Deploy version. ",(0,a.kt)("br",null),"\nIt might work with one minor version up or down, but there is no guarantee.  "))),(0,a.kt)("h2",{id:"running-the-integration-server"},"Running the integration server"),(0,a.kt)("ul",null,(0,a.kt)("li",{parentName:"ul"},(0,a.kt)("inlineCode",{parentName:"li"},"./gradlew startIntegrationServer")," - starts the server"),(0,a.kt)("li",{parentName:"ul"},(0,a.kt)("inlineCode",{parentName:"li"},"./gradlew clean startIntegrationServer")," - cleans previously generated files/folders and then starts the server"),(0,a.kt)("li",{parentName:"ul"},(0,a.kt)("inlineCode",{parentName:"li"},"./gradlew startIntegrationServer --stacktrace")," - starts the server and in case of any issues will display a stacktrace for troubleshooting. "),(0,a.kt)("li",{parentName:"ul"},(0,a.kt)("inlineCode",{parentName:"li"},"./gradlew startIntegrationServer --stacktrace -Dorg.gradle.debug=true --no-daemon")," - starts the server in debug mode.\nIn this mode you have to attach on remote debugging port 5005, it will wait before starting the server. It's useful if you have to debug\nthe plugin.")))}d.isMDXComponent=!0}}]);