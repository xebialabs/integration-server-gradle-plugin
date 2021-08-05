package com.xebialabs.gradle.integration.util

import org.gradle.api.Project

import java.nio.file.Paths

class WorkerUtil {

    static def isWorkerEnabled(Project project) {
        ExtensionsUtil.getExtension(project).externalWorker
    }

    static def isWorkerDirLocal(Project project) {
     if (ExtensionsUtil.getExtension(project).serverRuntimeDirectory == null){
         ExtensionsUtil.getExtension(project).workerDirLocal
     } else {
         true
     }
    }



    static def getExternalWorkerDir(project) {
            def serverVersion = ExtensionsUtil.getExtension(project).serverVersion
            def targetDir = project.buildDir.toPath().resolve(PluginUtil.DIST_DESTINATION_NAME).toAbsolutePath().toString()
            Paths.get(targetDir, "xl-deploy-${serverVersion}-worker").toAbsolutePath().toString()
    }

}
