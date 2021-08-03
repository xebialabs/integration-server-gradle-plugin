package com.xebialabs.gradle.integration.util

import org.gradle.api.Project

import java.nio.file.Paths

class WorkerUtil {

    static def isWorkerEnabled(Project project) {
        project.hasProperty("externalWorker") ?
                project.getProperty("externalWorker").toBoolean() : false
    }

    static def isWorkerDirLocal(Project project) {
        project.hasProperty("workerDirLocal") ?
                project.getProperty("workerDirLocal").toBoolean() : true && ExtensionsUtil.getServerWorkingDir(project) == null
    }

    static def getWorkerDir(project) {
        if (!isWorkerDirLocal(project)) {
            getWorkerRunTimeDirectory(project)
        } else {
            ExtensionsUtil.getServerWorkingDir(project)
        }
    }

    static def getWorkerRunTimeDirectory(Project project) {
        project.hasProperty("workerRuntimeDirectory") ?
                project.getProperty("workerRuntimeDirectory") : getExternalWorkerDir(project)
    }


    static def getExternalWorkerDir(project) {
        def serverVersion = ExtensionsUtil.getExtension(project).serverVersion
        def targetDir = project.buildDir.toPath().resolve(PluginUtil.DIST_DESTINATION_NAME).toAbsolutePath().toString()
        Paths.get(targetDir, "xl-deploy-${serverVersion}-worker").toAbsolutePath().toString()
    }

}
