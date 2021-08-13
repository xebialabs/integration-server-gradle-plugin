package ai.digital.integration.server.util


import ai.digital.integration.server.domain.Worker
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

import java.nio.file.Paths

class WorkerUtil {

    static def hasWorkers(Project project) {
        ExtensionUtil.getExtension(project).workers.size() > 0
    }

    static def getWorkerDir(Worker worker, Project project) {
        worker.directory != null && !worker.directory.isEmpty() ? worker.directory : ServerUtil.getServerWorkingDir(project)
    }

    static void copyServerDirToWorkerDir(Worker worker, Project project) {
        def sourceDir = Paths.get(ServerUtil.getServerWorkingDir(project)).toFile()
        def destinationDir = Paths.get(worker.directory).toFile()
        destinationDir.setExecutable(true)
        FileUtils.copyDirectory(sourceDir, destinationDir)
        ProcessUtil.chMod(project, "755", "${destinationDir.getAbsolutePath().toString()}")
    }

    static def isExternalWorker(Worker worker) {
        worker.directory != null && !worker.directory.isEmpty()
    }

    static def hasRuntimeDirectory(Project project) {
        ServerUtil.getServer(project).runtimeDirectory != null
    }

}
