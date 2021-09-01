package ai.digital.integration.server.util

import ai.digital.integration.server.domain.Worker
import org.gradle.api.Project

import java.nio.file.Paths

class WorkerUtil {

    static def hasWorkers(Project project) {
        ExtensionUtil.getExtension(project).workers.size() > 0
    }

    static List<Worker> getWorkers(Project project) {
        ExtensionUtil.getExtension(project).workers.collect { Worker worker ->
            enrichWorker(project, worker)
        }
    }

    private static Worker enrichWorker(Project project, Worker worker) {
        worker.setDebugPort(getDebugPort(project, worker))
        worker.setVersion(getWorkerVersion(project, worker))
        worker
    }

    static def getWorkerWorkingDir(Worker worker, Project project) {
        if (worker.runtimeDirectory == null) {
            def targetDir = IntegrationServerUtil.getDist(project)
            Paths.get(targetDir, worker.name, "deploy-task-engine-${worker.version}").toAbsolutePath().toString()
        } else {
            def target = project.projectDir.toString()
            Paths.get(target, worker.runtimeDirectory).toAbsolutePath().toString()
        }
    }

    static def isExternalRuntimeWorker(Worker worker, Project project) {
        worker.runtimeDirectory != null && !worker.runtimeDirectory.isEmpty() && worker.runtimeDirectory != ServerUtil.getServerWorkingDir(project)
    }

    static def isDistDownloadRequired(Worker worker) {
        worker.runtimeDirectory == null
    }

    static def hasRuntimeDirectory(Worker worker) {
        worker.runtimeDirectory != null
    }

    private static String getWorkerVersion(Project project, Worker worker) {
        project.hasProperty("xlWorkerVersion") ? project.property("xlWorkerVersion") : worker.version
    }

    private static Integer getDebugPort(Project project, Worker worker) {
        project.hasProperty("workerDebugPort") ? Integer.valueOf(project.property("workerDebugPort").toString()) : worker.debugPort
    }
}
