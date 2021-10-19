"use strict";(self.webpackChunkdocs=self.webpackChunkdocs||[]).push([[671],{3905:function(e,t,n){n.d(t,{Zo:function(){return s},kt:function(){return d}});var r=n(7294);function o(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function i(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);t&&(r=r.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,r)}return n}function a(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?i(Object(n),!0).forEach((function(t){o(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):i(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function l(e,t){if(null==e)return{};var n,r,o=function(e,t){if(null==e)return{};var n,r,o={},i=Object.keys(e);for(r=0;r<i.length;r++)n=i[r],t.indexOf(n)>=0||(o[n]=e[n]);return o}(e,t);if(Object.getOwnPropertySymbols){var i=Object.getOwnPropertySymbols(e);for(r=0;r<i.length;r++)n=i[r],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(o[n]=e[n])}return o}var u=r.createContext({}),c=function(e){var t=r.useContext(u),n=t;return e&&(n="function"==typeof e?e(t):a(a({},t),e)),n},s=function(e){var t=c(e.components);return r.createElement(u.Provider,{value:t},e.children)},p={inlineCode:"code",wrapper:function(e){var t=e.children;return r.createElement(r.Fragment,{},t)}},f=r.forwardRef((function(e,t){var n=e.components,o=e.mdxType,i=e.originalType,u=e.parentName,s=l(e,["components","mdxType","originalType","parentName"]),f=c(n),d=o,m=f["".concat(u,".").concat(d)]||f[d]||p[d]||i;return n?r.createElement(m,a(a({ref:t},s),{},{components:n})):r.createElement(m,a({ref:t},s))}));function d(e,t){var n=arguments,o=t&&t.mdxType;if("string"==typeof e||o){var i=n.length,a=new Array(i);a[0]=f;var l={};for(var u in t)hasOwnProperty.call(t,u)&&(l[u]=t[u]);l.originalType=e,l.mdxType="string"==typeof e?e:o,a[1]=l;for(var c=2;c<i;c++)a[c]=n[c];return r.createElement.apply(null,a)}return r.createElement.apply(null,n)}f.displayName="MDXCreateElement"},9881:function(e,t,n){n.r(t),n.d(t,{frontMatter:function(){return l},contentTitle:function(){return u},metadata:function(){return c},toc:function(){return s},default:function(){return f}});var r=n(7462),o=n(3366),i=(n(7294),n(3905)),a=["components"],l={sidebar_position:1},u="Introduction",c={unversionedId:"intro",id:"intro",isDocsHomePage:!1,title:"Introduction",description:"Are you developing your own plugin for Digital.ai Deploy or Release and looking for a simple way",source:"@site/docs/intro.md",sourceDirName:".",slug:"/intro",permalink:"/integration-server-gradle-plugin/docs/intro",tags:[],version:"current",sidebarPosition:1,frontMatter:{sidebar_position:1},sidebar:"tutorialSidebar",next:{title:"Installation",permalink:"/integration-server-gradle-plugin/docs/deploy/installation"}},s=[],p={toc:s};function f(e){var t=e.components,n=(0,o.Z)(e,a);return(0,i.kt)("wrapper",(0,r.Z)({},p,n,{components:t,mdxType:"MDXLayout"}),(0,i.kt)("h1",{id:"introduction"},"Introduction"),(0,i.kt)("p",null,"Are you developing your own plugin for ",(0,i.kt)("strong",{parentName:"p"},"Digital.ai Deploy")," or ",(0,i.kt)("strong",{parentName:"p"},"Release")," and looking for a simple way\nto run integration tests against it? ",(0,i.kt)("br",null),"\nThen you are on the right track! ",(0,i.kt)("br",null)),(0,i.kt)("p",null,(0,i.kt)("strong",{parentName:"p"},"Integration Server")," will help you to setup and run Deploy of your preferred version (10.2+) in different ways, by: ",(0,i.kt)("br",null)),(0,i.kt)("ul",null,(0,i.kt)("li",{parentName:"ul"},"downloading it from your private Nexus repository"),(0,i.kt)("li",{parentName:"ul"},"Running from the specified folder "),(0,i.kt)("li",{parentName:"ul"},"pulling a docker image. ")),(0,i.kt)("p",null,"You can:"),(0,i.kt)("ul",null,(0,i.kt)("li",{parentName:"ul"},"customise the any files"),(0,i.kt)("li",{parentName:"ul"},"add libraries and plugins you wish"),(0,i.kt)("li",{parentName:"ul"},"do easy YAML patches for central configuration files"),(0,i.kt)("li",{parentName:"ul"},"run workers and satellites (and also in a debug mode) ")),(0,i.kt)("p",null,"In only a matter of describing your setup in declarative way.\nThe heavy lifting is done by the plugin and about to get more features and help in a mid/long term. "),(0,i.kt)("p",null,"If happens that currently you are missing some feature, it's not a problem, you can extend it. ",(0,i.kt)("br",null),"\nBecause ",(0,i.kt)("strong",{parentName:"p"},"Integration Server")," built as a Gradle plugin and very flexible for adjustments.\nMoreover, code is open, and you can fork it, or create PRs which we will review and might consider merging it,\nif it is done generic and can be useful for other users/customers too."))}f.isMDXComponent=!0}}]);