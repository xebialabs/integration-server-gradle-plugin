package com.xebialabs.gradle.integration.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory

class ServerConfUtil {

    static def mapper = new ObjectMapper(new YAMLFactory())


    static def configFile(project) {
        return ServerConfUtil.class.classLoader.getResourceAsStream("server-conf/deploy-server.yaml")
    }

    static def serverConfig(project, akkaPort) {
        def root = mapper.readTree(configFile(project))
        root.put("deploy.server.port", akkaPort)
        return root;
    }
}
