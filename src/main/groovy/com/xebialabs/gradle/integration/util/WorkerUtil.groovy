package com.xebialabs.gradle.integration.util

import org.gradle.api.Project

import java.nio.file.Paths

class WorkerUtil {

    static def isWorkerEnabled(Project project) {
        project.hasProperty("externalWorker") ?
                project.getProperty("externalWorker").toBoolean() : false
    }

    static def isLocalWorker(Project project) {
        project.hasProperty("localWorker") ?
                project.getProperty("localWorker").toBoolean() : true
    }

    static def getWorkerDir(project) {
        if (isLocalWorker(project)) {
            ExtensionsUtil.getServerWorkingDir(project)
        } else {
            getExternalWorkerDir(project)
        }
    }

    static def getExternalWorkerDir(project) {
        def serverVersion = ExtensionsUtil.getExtension(project).serverVersion
        def targetDir = project.buildDir.toPath().resolve(PluginUtil.DIST_DESTINATION_NAME).toAbsolutePath().toString()
        Paths.get(targetDir, "xl-deploy-${serverVersion}-worker").toAbsolutePath().toString()
    }

}
