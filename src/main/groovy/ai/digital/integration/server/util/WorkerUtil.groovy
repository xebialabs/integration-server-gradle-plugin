package ai.digital.integration.server.util

import ai.digital.integration.server.domain.Worker
import org.gradle.api.Project

import java.nio.file.Paths

class WorkerUtil {

    static def hasWorkers(Project project) {
        ExtensionUtil.getExtension(project).workers.size() > 0
    }

    static def hasSlimWorkers(Project project) {
        getWorkers(project).any {worker ->
            worker.slimDistribution
        }
    }

    static def hasNonSlimWorkers(Project project) {
        getWorkers(project).any {worker ->
            !worker.slimDistribution
        }
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

    static def getWorkerWorkingDir(Project project, Worker worker) {
        if (worker.runtimeDirectory == null) {
            def targetDir = IntegrationServerUtil.getDist(project)
            String prefix = worker.slimDistribution ? "xl-deploy-worker" : "deploy-task-engine"
            Paths.get(targetDir, worker.name, "${prefix}-${worker.version}").toAbsolutePath().toString()
        } else {
            def target = project.projectDir.toString()
            Paths.get(target, worker.runtimeDirectory).toAbsolutePath().toString()
        }
    }

    static def isExternalRuntimeWorker(Project project, Worker worker) {
        worker.runtimeDirectory != null && !worker.runtimeDirectory.isEmpty() && worker.runtimeDirectory != ServerUtil.getServerWorkingDir(project)
    }

    static def isDistDownloadRequired(Worker worker) {
        worker.runtimeDirectory == null
    }

    static def hasRuntimeDirectory(Worker worker) {
        worker.runtimeDirectory != null
    }

    private static String getWorkerVersion(Project project, Worker worker) {
        if (project.hasProperty("xlWorkerVersion")) {
            project.getProperty("xlWorkerVersion")
        } else if (worker.version?.trim()) {
            worker.version
        } else if (ServerUtil.getServer(project).version) {
            ServerUtil.getServer(project).version
        } else {
            project.logger.error("Worker Version is not specified")
            System.exit(1)
            return null
        }
    }

    private static Integer getDebugPort(Project project, Worker worker) {
        project.hasProperty("workerDebugPort") ? Integer.valueOf(project.property("workerDebugPort").toString()) : worker.debugPort
    }
}
