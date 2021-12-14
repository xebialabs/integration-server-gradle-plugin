"use strict";(self.webpackChunkdocs=self.webpackChunkdocs||[]).push([[1133],{3905:function(e,t,n){n.d(t,{Zo:function(){return p},kt:function(){return g}});var a=n(7294);function r(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function i(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);t&&(a=a.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,a)}return n}function o(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?i(Object(n),!0).forEach((function(t){r(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):i(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function l(e,t){if(null==e)return{};var n,a,r=function(e,t){if(null==e)return{};var n,a,r={},i=Object.keys(e);for(a=0;a<i.length;a++)n=i[a],t.indexOf(n)>=0||(r[n]=e[n]);return r}(e,t);if(Object.getOwnPropertySymbols){var i=Object.getOwnPropertySymbols(e);for(a=0;a<i.length;a++)n=i[a],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(r[n]=e[n])}return r}var s=a.createContext({}),d=function(e){var t=a.useContext(s),n=t;return e&&(n="function"==typeof e?e(t):o(o({},t),e)),n},p=function(e){var t=d(e.components);return a.createElement(s.Provider,{value:t},e.children)},c={inlineCode:"code",wrapper:function(e){var t=e.children;return a.createElement(a.Fragment,{},t)}},m=a.forwardRef((function(e,t){var n=e.components,r=e.mdxType,i=e.originalType,s=e.parentName,p=l(e,["components","mdxType","originalType","parentName"]),m=d(n),g=r,u=m["".concat(s,".").concat(g)]||m[g]||c[g]||i;return n?a.createElement(u,o(o({ref:t},p),{},{components:n})):a.createElement(u,o({ref:t},p))}));function g(e,t){var n=arguments,r=t&&t.mdxType;if("string"==typeof e||r){var i=n.length,o=new Array(i);o[0]=m;var l={};for(var s in t)hasOwnProperty.call(t,s)&&(l[s]=t[s]);l.originalType=e,l.mdxType="string"==typeof e?e:r,o[1]=l;for(var d=2;d<i;d++)o[d]=n[d];return a.createElement.apply(null,o)}return a.createElement.apply(null,n)}m.displayName="MDXCreateElement"},6451:function(e,t,n){n.r(t),n.d(t,{frontMatter:function(){return l},contentTitle:function(){return s},metadata:function(){return d},toc:function(){return p},default:function(){return m}});var a=n(7462),r=n(3366),i=(n(7294),n(3905)),o=["components"],l={sidebar_position:7},s="Flags",d={unversionedId:"getting-started/flags",id:"version-10.3.0/getting-started/flags",isDocsHomePage:!1,title:"Flags",description:"Most of the flags (apart from database) you can define in configuration section.",source:"@site/versioned_docs/version-10.3.0/getting-started/flags.md",sourceDirName:"getting-started",slug:"/getting-started/flags",permalink:"/integration-server-gradle-plugin/docs/10.3.0/getting-started/flags",tags:[],version:"10.3.0",sidebarPosition:7,frontMatter:{sidebar_position:7},sidebar:"version-10.4.0/tutorialSidebar",previous:{title:"Tasks",permalink:"/integration-server-gradle-plugin/docs/10.3.0/getting-started/tasks"},next:{title:"Limitations",permalink:"/integration-server-gradle-plugin/docs/10.3.0/getting-started/limitations"}},p=[{value:"Database flag",id:"database-flag",children:[{value:"Derby",id:"derby",children:[]}]}],c={toc:p};function m(e){var t=e.components,n=(0,r.Z)(e,o);return(0,i.kt)("wrapper",(0,a.Z)({},c,n,{components:t,mdxType:"MDXLayout"}),(0,i.kt)("h1",{id:"flags"},"Flags"),(0,i.kt)("p",null,"Most of the flags (apart from database) you can define in configuration section.",(0,i.kt)("br",null),"\nSo why then we need flags?",(0,i.kt)("br",null)),(0,i.kt)("p",null,"You can look at them as phantom configuration, which you don't want to have in your permanent configuration.",(0,i.kt)("br",null),"\nOr it can be used to not create a branching logic for databases. If we would keep it only in a configuration section,\nand you would like to run your tests against 3-5 databases, then you had to create some branching logic for that. ",(0,i.kt)("br",null),"\nAnd anyway, you can say your options to Gradle only via parameters. But I agree, that having a default database in\na configuration section make sense, and we have it in plans to implement."),(0,i.kt)("div",{className:"admonition admonition-note alert alert--secondary"},(0,i.kt)("div",{parentName:"div",className:"admonition-heading"},(0,i.kt)("h5",{parentName:"div"},(0,i.kt)("span",{parentName:"h5",className:"admonition-icon"},(0,i.kt)("svg",{parentName:"span",xmlns:"http://www.w3.org/2000/svg",width:"14",height:"16",viewBox:"0 0 14 16"},(0,i.kt)("path",{parentName:"svg",fillRule:"evenodd",d:"M6.3 5.69a.942.942 0 0 1-.28-.7c0-.28.09-.52.28-.7.19-.18.42-.28.7-.28.28 0 .52.09.7.28.18.19.28.42.28.7 0 .28-.09.52-.28.7a1 1 0 0 1-.7.3c-.28 0-.52-.11-.7-.3zM8 7.99c-.02-.25-.11-.48-.31-.69-.2-.19-.42-.3-.69-.31H6c-.27.02-.48.13-.69.31-.2.2-.3.44-.31.69h1v3c.02.27.11.5.31.69.2.2.42.31.69.31h1c.27 0 .48-.11.69-.31.2-.19.3-.42.31-.69H8V7.98v.01zM7 2.3c-3.14 0-5.7 2.54-5.7 5.68 0 3.14 2.56 5.7 5.7 5.7s5.7-2.55 5.7-5.7c0-3.15-2.56-5.69-5.7-5.69v.01zM7 .98c3.86 0 7 3.14 7 7s-3.14 7-7 7-7-3.12-7-7 3.14-7 7-7z"}))),"note")),(0,i.kt)("div",{parentName:"div",className:"admonition-content"},(0,i.kt)("p",{parentName:"div"},"Parameters/Flags can be defined in 2 ways:"),(0,i.kt)("ul",{parentName:"div"},(0,i.kt)("li",{parentName:"ul"},"In a command line: ",(0,i.kt)("inlineCode",{parentName:"li"},"./gradlew startIntegrationServer -Pdatabase=postgres-10")),(0,i.kt)("li",{parentName:"ul"},"In ",(0,i.kt)("inlineCode",{parentName:"li"},"gradle.properties")," in a root of your project, as a key value pair: ",(0,i.kt)("inlineCode",{parentName:"li"},"database=postgres-10"))))),(0,i.kt)("table",null,(0,i.kt)("thead",{parentName:"table"},(0,i.kt)("tr",{parentName:"thead"},(0,i.kt)("th",{parentName:"tr",align:"center"},"Flag name"),(0,i.kt)("th",{parentName:"tr",align:"center"},"Options"),(0,i.kt)("th",{parentName:"tr",align:"center"},"Description"))),(0,i.kt)("tbody",{parentName:"table"},(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"database"),(0,i.kt)("td",{parentName:"tr",align:"center"},(0,i.kt)("inlineCode",{parentName:"td"},"derby"),(0,i.kt)("br",null)," ",(0,i.kt)("inlineCode",{parentName:"td"},"derby-inmemory"),(0,i.kt)("br",null)," ",(0,i.kt)("inlineCode",{parentName:"td"},"derby-network"),(0,i.kt)("br",null)," ",(0,i.kt)("inlineCode",{parentName:"td"},"mssql"),(0,i.kt)("br",null)," ",(0,i.kt)("inlineCode",{parentName:"td"},"mysql"),(0,i.kt)("br",null)," ",(0,i.kt)("inlineCode",{parentName:"td"},"mysql-8"),(0,i.kt)("br",null)," ",(0,i.kt)("inlineCode",{parentName:"td"},"oracle-19c-se"),(0,i.kt)("br",null)," ",(0,i.kt)("inlineCode",{parentName:"td"},"postgres-10"),(0,i.kt)("br",null)," ",(0,i.kt)("inlineCode",{parentName:"td"},"postgres-12")),(0,i.kt)("td",{parentName:"tr",align:"center"},"Type of database. ",(0,i.kt)("a",{parentName:"td",href:"#database-flag"},"More details"))),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"debug"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Boolean"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Enables or disables starting processes in debug mode. It overrides any debug setting in configuration. If not set debugging is enabled.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"databasePort"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Any available port"),(0,i.kt)("td",{parentName:"tr",align:"center"},"The port on which database is going to be started.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"logSql"),(0,i.kt)("td",{parentName:"tr",align:"center"},"true/false"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Enables printing of SQL queries executed on the server")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"satelliteDebugPort"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Any available port"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Provides a satellite debug port for remote debugging.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"serverDebugPort"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Any available port"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Provides a server debug port for remote debugging.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"serverHttpPort"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Any available port"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Overrides default server HTTP port")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"testBaseSubDirectory"),(0,i.kt)("td",{parentName:"tr",align:"center"},"String"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Example: ",(0,i.kt)("inlineCode",{parentName:"td"},"-PtestBaseSubDirectory=provision-aws")," It points to a subset of tests running in a group of the tests")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"testName"),(0,i.kt)("td",{parentName:"tr",align:"center"},"String"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Example: ",(0,i.kt)("inlineCode",{parentName:"td"},"-PtestName=azure"),". If to specify this parameter, only 1 test section will be executed. It can be helpful, if you run in your CI pipeline tests in parallel and define in each the group of tests to run.")),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"testScriptPattern"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Pattern"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Example: ",(0,i.kt)("inlineCode",{parentName:"td"},"-PtestScriptPattern=provision-aws/provision_aws.py"))),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"testSetupScripts"),(0,i.kt)("td",{parentName:"tr",align:"center"},"String"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Example: ",(0,i.kt)("inlineCode",{parentName:"td"},"-PtestScriptPatterns=root/setup.py,root/gcp/setup.py"))),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:"center"},"testTeardownScripts"),(0,i.kt)("td",{parentName:"tr",align:"center"},"String"),(0,i.kt)("td",{parentName:"tr",align:"center"},"Example: ",(0,i.kt)("inlineCode",{parentName:"td"},"-PtestTeardownScripts=root/teardown.py,root/gcp/teardown.py"))))),(0,i.kt)("div",{className:"admonition admonition-info alert alert--info"},(0,i.kt)("div",{parentName:"div",className:"admonition-heading"},(0,i.kt)("h5",{parentName:"div"},(0,i.kt)("span",{parentName:"h5",className:"admonition-icon"},(0,i.kt)("svg",{parentName:"span",xmlns:"http://www.w3.org/2000/svg",width:"14",height:"16",viewBox:"0 0 14 16"},(0,i.kt)("path",{parentName:"svg",fillRule:"evenodd",d:"M7 2.3c3.14 0 5.7 2.56 5.7 5.7s-2.56 5.7-5.7 5.7A5.71 5.71 0 0 1 1.3 8c0-3.14 2.56-5.7 5.7-5.7zM7 1C3.14 1 0 4.14 0 8s3.14 7 7 7 7-3.14 7-7-3.14-7-7-7zm1 3H6v5h2V4zm0 6H6v2h2v-2z"}))),"info")),(0,i.kt)("div",{parentName:"div",className:"admonition-content"},(0,i.kt)("p",{parentName:"div"},"In case when the configuration is defined in ",(0,i.kt)("inlineCode",{parentName:"p"},"build.gradle")," and a parameter provided, parameter will take a precedence. "))),(0,i.kt)("h2",{id:"database-flag"},"Database flag"),(0,i.kt)("p",null,"Each database configuration is fixed, you can't modify it through the configuration of flags. ",(0,i.kt)("br",null),"\nThe only thing what you can modify is the port for ",(0,i.kt)("inlineCode",{parentName:"p"},"derby")," database. ",(0,i.kt)("br",null),"\nWe have plans to make it possible to choose the port for any database. ",(0,i.kt)("br",null)),(0,i.kt)("p",null,"What exactly is configured for the database, you can check in ",(0,i.kt)("inlineCode",{parentName:"p"},"src/main/resources/database-conf"),", or when you run the\n",(0,i.kt)("em",{parentName:"p"},"Integration Server")," in your ",(0,i.kt)("inlineCode",{parentName:"p"},"<DEPLOY_HOME>/centralConfiguration/deploy-repository.yaml")," file."),(0,i.kt)("h3",{id:"derby"},"Derby"),(0,i.kt)("p",null,"You can run derby in 2 modes, in-memory or from the file system. You should know that derby in-memory has also limitations\nregarding the connection limitation. It can be only one, so you can't view the content of the table."),(0,i.kt)("p",null,(0,i.kt)("inlineCode",{parentName:"p"},"derby")," is an alias for ",(0,i.kt)("inlineCode",{parentName:"p"},"derby-network"),"."))}m.isMDXComponent=!0}}]);