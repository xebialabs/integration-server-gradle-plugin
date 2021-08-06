package com.xebialabs.gradle.integration.util

import com.xebialabs.gradle.integration.Worker
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

import java.nio.file.Paths

class WorkerUtil {

    static def hasWorkers(Project project) {
        ExtensionsUtil.getExtension(project).workers.size() > 0
    }

    static def getWorkerDir(Worker worker, Project project) {
        worker.directory != null && !worker.directory.isEmpty() ? worker.directory : ExtensionsUtil.getServerWorkingDir(project)
    }

    static void copyServerDirToWorkerDir(worker, project) {
        def sourceDir = Paths.get(ExtensionsUtil.getServerWorkingDir(project)).toFile()
        def destinationDir = Paths.get(worker.directory).toFile()
        destinationDir.setExecutable(true)
        FileUtils.copyDirectory(sourceDir, destinationDir);
        ProcessUtil.chMod(project, "755", "${destinationDir.getAbsolutePath().toString()}")
    }

    static def isExternalWorker(Worker worker) {
        return worker.directory != null && !worker.directory.isEmpty()
    }

    static def hasRuntimeDirectory(Project project) {
        return ExtensionsUtil.getExtension(project).serverRuntimeDirectory != null
    }

}
