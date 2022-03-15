"use strict";(self.webpackChunkdocs=self.webpackChunkdocs||[]).push([[4844],{3905:function(e,t,n){n.d(t,{Zo:function(){return p},kt:function(){return d}});var i=n(7294);function r(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function a(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var i=Object.getOwnPropertySymbols(e);t&&(i=i.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,i)}return n}function o(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?a(Object(n),!0).forEach((function(t){r(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):a(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function l(e,t){if(null==e)return{};var n,i,r=function(e,t){if(null==e)return{};var n,i,r={},a=Object.keys(e);for(i=0;i<a.length;i++)n=a[i],t.indexOf(n)>=0||(r[n]=e[n]);return r}(e,t);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);for(i=0;i<a.length;i++)n=a[i],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(r[n]=e[n])}return r}var s=i.createContext({}),u=function(e){var t=i.useContext(s),n=t;return e&&(n="function"==typeof e?e(t):o(o({},t),e)),n},p=function(e){var t=u(e.components);return i.createElement(s.Provider,{value:t},e.children)},c={inlineCode:"code",wrapper:function(e){var t=e.children;return i.createElement(i.Fragment,{},t)}},m=i.forwardRef((function(e,t){var n=e.components,r=e.mdxType,a=e.originalType,s=e.parentName,p=l(e,["components","mdxType","originalType","parentName"]),m=u(n),d=r,f=m["".concat(s,".").concat(d)]||m[d]||c[d]||a;return n?i.createElement(f,o(o({ref:t},p),{},{components:n})):i.createElement(f,o({ref:t},p))}));function d(e,t){var n=arguments,r=t&&t.mdxType;if("string"==typeof e||r){var a=n.length,o=new Array(a);o[0]=m;var l={};for(var s in t)hasOwnProperty.call(t,s)&&(l[s]=t[s]);l.originalType=e,l.mdxType="string"==typeof e?e:r,o[1]=l;for(var u=2;u<a;u++)o[u]=n[u];return i.createElement.apply(null,o)}return i.createElement.apply(null,n)}m.displayName="MDXCreateElement"},3256:function(e,t,n){n.r(t),n.d(t,{frontMatter:function(){return l},contentTitle:function(){return s},metadata:function(){return u},toc:function(){return p},default:function(){return m}});var i=n(7462),r=n(3366),a=(n(7294),n(3905)),o=["components"],l={sidebar_position:8},s="Limitations",u={unversionedId:"getting-started/limitations",id:"version-10.3.0/getting-started/limitations",isDocsHomePage:!1,title:"Limitations",description:"Docker setup limitations",source:"@site/versioned_docs/version-10.3.0/getting-started/limitations.md",sourceDirName:"getting-started",slug:"/getting-started/limitations",permalink:"/integration-server-gradle-plugin/docs/10.3.0/getting-started/limitations",tags:[],version:"10.3.0",sidebarPosition:8,frontMatter:{sidebar_position:8},sidebar:"version-10.4.0/tutorialSidebar",previous:{title:"Flags",permalink:"/integration-server-gradle-plugin/docs/10.3.0/getting-started/flags"},next:{title:"Development",permalink:"/integration-server-gradle-plugin/docs/10.3.0/getting-started/development"}},p=[{value:"Docker setup limitations",id:"docker-setup-limitations",children:[]},{value:"Data Import limitation (available only for the internal use in Digital.ai)",id:"data-import-limitation-available-only-for-the-internal-use-in-digitalai",children:[]},{value:"Database Images limitations",id:"database-images-limitations",children:[]}],c={toc:p};function m(e){var t=e.components,n=(0,r.Z)(e,o);return(0,a.kt)("wrapper",(0,i.Z)({},c,n,{components:t,mdxType:"MDXLayout"}),(0,a.kt)("h1",{id:"limitations"},"Limitations"),(0,a.kt)("h2",{id:"docker-setup-limitations"},"Docker setup limitations"),(0,a.kt)("p",null,"What docker setup doesn't support:"),(0,a.kt)("ul",null,(0,a.kt)("li",{parentName:"ul"},"To run workers"),(0,a.kt)("li",{parentName:"ul"},"To run satellites"),(0,a.kt)("li",{parentName:"ul"},"Overlay works only mounted folders, so it is: conf, centralConfiguration, hotfix, plugins."),(0,a.kt)("li",{parentName:"ul"},"Debugging"),(0,a.kt)("li",{parentName:"ul"},"Log levels")),(0,a.kt)("p",null,"Docker image contains all plugins which are defined in Deploy Server Trial distribution.\nIf you want to exclude some of them you can use property ",(0,a.kt)("inlineCode",{parentName:"p"},"defaultOfficialPluginsToExclude"),".\nFor example if you want to exclude terraform and aws plugin, you have to configure as: "),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-groovy"},'defaultOfficialPluginsToExclude = ["terraform", "aws"]\n')),(0,a.kt)("h2",{id:"data-import-limitation-available-only-for-the-internal-use-in-digitalai"},"Data Import limitation (available only for the internal use in Digital.ai)"),(0,a.kt)("ul",null,(0,a.kt)("li",{parentName:"ul"},(0,a.kt)("inlineCode",{parentName:"li"},"postgres")," is the only database which fully support data import"),(0,a.kt)("li",{parentName:"ul"},(0,a.kt)("inlineCode",{parentName:"li"},"derby-inmemory"),", ",(0,a.kt)("inlineCode",{parentName:"li"},"derby-network")," do not support DbUnit data import,\nas these databases are not supported, use old data export format")),(0,a.kt)("h2",{id:"database-images-limitations"},"Database Images limitations"),(0,a.kt)("ul",null,(0,a.kt)("li",{parentName:"ul"},"Only  ",(0,a.kt)("inlineCode",{parentName:"li"},"mysql"),", ",(0,a.kt)("inlineCode",{parentName:"li"},"mysql-8"),", ",(0,a.kt)("inlineCode",{parentName:"li"},"postgres")," can be started at the moment with the integration server"),(0,a.kt)("li",{parentName:"ul"},(0,a.kt)("inlineCode",{parentName:"li"},"mssql"),", ",(0,a.kt)("inlineCode",{parentName:"li"},"oracle-19c-se")," require building an image at the moment and cannot be started by the integration server")))}m.isMDXComponent=!0}}]);