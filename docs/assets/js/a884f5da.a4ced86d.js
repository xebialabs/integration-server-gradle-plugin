"use strict";(self.webpackChunkdocs=self.webpackChunkdocs||[]).push([[9241],{3905:function(e,t,r){r.d(t,{Zo:function(){return s},kt:function(){return f}});var n=r(7294);function o(e,t,r){return t in e?Object.defineProperty(e,t,{value:r,enumerable:!0,configurable:!0,writable:!0}):e[t]=r,e}function i(e,t){var r=Object.keys(e);if(Object.getOwnPropertySymbols){var n=Object.getOwnPropertySymbols(e);t&&(n=n.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),r.push.apply(r,n)}return r}function a(e){for(var t=1;t<arguments.length;t++){var r=null!=arguments[t]?arguments[t]:{};t%2?i(Object(r),!0).forEach((function(t){o(e,t,r[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(r)):i(Object(r)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(r,t))}))}return e}function l(e,t){if(null==e)return{};var r,n,o=function(e,t){if(null==e)return{};var r,n,o={},i=Object.keys(e);for(n=0;n<i.length;n++)r=i[n],t.indexOf(r)>=0||(o[r]=e[r]);return o}(e,t);if(Object.getOwnPropertySymbols){var i=Object.getOwnPropertySymbols(e);for(n=0;n<i.length;n++)r=i[n],t.indexOf(r)>=0||Object.prototype.propertyIsEnumerable.call(e,r)&&(o[r]=e[r])}return o}var c=n.createContext({}),u=function(e){var t=n.useContext(c),r=t;return e&&(r="function"==typeof e?e(t):a(a({},t),e)),r},s=function(e){var t=u(e.components);return n.createElement(c.Provider,{value:t},e.children)},p={inlineCode:"code",wrapper:function(e){var t=e.children;return n.createElement(n.Fragment,{},t)}},d=n.forwardRef((function(e,t){var r=e.components,o=e.mdxType,i=e.originalType,c=e.parentName,s=l(e,["components","mdxType","originalType","parentName"]),d=u(r),f=o,y=d["".concat(c,".").concat(f)]||d[f]||p[f]||i;return r?n.createElement(y,a(a({ref:t},s),{},{components:r})):n.createElement(y,a({ref:t},s))}));function f(e,t){var r=arguments,o=t&&t.mdxType;if("string"==typeof e||o){var i=r.length,a=new Array(i);a[0]=d;var l={};for(var c in t)hasOwnProperty.call(t,c)&&(l[c]=t[c]);l.originalType=e,l.mdxType="string"==typeof e?e:o,a[1]=l;for(var u=2;u<i;u++)a[u]=r[u];return n.createElement.apply(null,a)}return n.createElement.apply(null,r)}d.displayName="MDXCreateElement"},7322:function(e,t,r){r.r(t),r.d(t,{frontMatter:function(){return l},contentTitle:function(){return c},metadata:function(){return u},toc:function(){return s},default:function(){return d}});var n=r(7462),o=r(3366),i=(r(7294),r(3905)),a=["components"],l={sidebar_position:3},c="Architecture",u={unversionedId:"deploy/architecture",id:"deploy/architecture",isDocsHomePage:!1,title:"Architecture",description:"There are 3 ways how your can install Deploy",source:"@site/docs/deploy/architecture.md",sourceDirName:"deploy",slug:"/deploy/architecture",permalink:"/integration-server-gradle-plugin/docs/deploy/architecture",tags:[],version:"current",sidebarPosition:3,frontMatter:{sidebar_position:3},sidebar:"tutorialSidebar",previous:{title:"Plugin structure",permalink:"/integration-server-gradle-plugin/docs/deploy/structure"},next:{title:"Examples",permalink:"/integration-server-gradle-plugin/docs/deploy/examples"}},s=[],p={toc:s};function d(e){var t=e.components,l=(0,o.Z)(e,a);return(0,i.kt)("wrapper",(0,n.Z)({},p,l,{components:t,mdxType:"MDXLayout"}),(0,i.kt)("h1",{id:"architecture"},"Architecture"),(0,i.kt)("p",null,"There are 3 ways how your can install Deploy"),(0,i.kt)("p",null,(0,i.kt)("img",{alt:"Types of installation",src:r(6013).Z})),(0,i.kt)("p",null,"What kind of setup going to be chosen, depends on the filled in configuration fields. ",(0,i.kt)("br",null)),(0,i.kt)("p",null,"By default, it checks the Nexus in location: ",(0,i.kt)("inlineCode",{parentName:"p"},"com.xebialabs.deployit:xl-deploy-base:${server.version}:server@zip"),".\nYou have to keep it there in your private Nexus in order the plugin could pull it."),(0,i.kt)("p",null,"Runtime directory used when you want to run Deploy for a specific folder and can attach on the fly your java modules\nwith help of Gradle (can be useful in case of adding some test or mocking modules). It can be enabled by providing the\nvalue for the field ",(0,i.kt)("inlineCode",{parentName:"p"},"runtimeDirectory")," in servers section.   "),(0,i.kt)("p",null,"Docker based installation is going to happen if to fill in the field ",(0,i.kt)("inlineCode",{parentName:"p"},"dockerImage")," in servers section.   "),(0,i.kt)("p",null,"The richest setup available now you can configure with help of ",(0,i.kt)("strong",{parentName:"p"},"Integration Server")," is depicted below."),(0,i.kt)("p",null,(0,i.kt)("img",{alt:"Richest setup available",src:r(2732).Z})),(0,i.kt)("p",null,"MQ and Database setup done based on Docker. Only Derby is exception, it runs as a JDK process."))}d.isMDXComponent=!0},2732:function(e,t,r){t.Z=r.p+"assets/images/richest-setup-available-395ea75ef20f0459b836f123d7078fe4.jpg"},6013:function(e,t,r){t.Z=r.p+"assets/images/types-of-installation-ede36dc765f62483ac676f49ae66352f.jpg"}}]);