package ai.digital.integration.server.util

import ai.digital.integration.server.domain.AkkaSecured
import ai.digital.integration.server.domain.Worker
import org.gradle.api.Project
import java.nio.file.Paths
import kotlin.system.exitProcess

class WorkerUtil {
    companion object {
        @JvmStatic
        fun hasWorkers(project: Project): Boolean {
            return ExtensionUtil.getExtension(project).workers.size > 0
        }

        @JvmStatic
        fun hasSlimWorkers(project: Project): Boolean {
            return getWorkers(project).any { worker ->
                worker.slimDistribution
            }
        }

        @JvmStatic
        fun hasNonSlimWorkers(project: Project): Boolean {
            return getWorkers(project).any { worker ->
                !worker.slimDistribution
            }
        }

        @JvmStatic
        fun getWorkers(project: Project): List<Worker> {
            return ExtensionUtil.getExtension(project).workers.map { worker: Worker ->
                enrichWorker(project, worker)
            }
        }

        @JvmStatic
        private fun enrichWorker(project: Project, worker: Worker): Worker {
            worker.debugPort = getDebugPort(project, worker)
            worker.version = getWorkerVersion(project, worker)
            return worker
        }

        @JvmStatic
        fun getWorkerWorkingDir(project: Project, worker: Worker): String {
            return if (getRuntimeDirectory(project, worker) == null) {
                val targetDir = IntegrationServerUtil.getDist(project)
                val prefix = if (worker.slimDistribution) "deploy-task-engine" else "xl-deploy-worker"
                Paths.get(targetDir, worker.name, "${prefix}-${worker.version}").toAbsolutePath().toString()
            } else {
                val target = project.projectDir.toString()
                Paths.get(target, getRuntimeDirectory(project, worker)).toAbsolutePath().toString()
            }
        }

        @JvmStatic
        fun isExternalRuntimeWorker(project: Project, worker: Worker): Boolean {
            return getRuntimeDirectory(project, worker) == null ||
                    (!getRuntimeDirectory(project, worker).isNullOrEmpty() &&
                            getWorkerWorkingDir(project, worker) != DeployServerUtil.getServerWorkingDir(project))
        }

        @JvmStatic
        fun isDistDownloadRequired(project: Project, worker: Worker): Boolean {
            return getRuntimeDirectory(project, worker) == null
        }

        @JvmStatic
        fun composeProgramParams(project: Project, worker: Worker, hostName: String, port: String, useWorkerCommand: Boolean): List<String> {

            val params = mutableListOf(
                    "-master",
                    "127.0.0.1:$port",
                    "-api",
                    DeployServerUtil.getUrl(project),
                    "-hostname",
                    hostName,
                    "-name",
                    worker.name,
                    "-port",
                    worker.port
            )

            if (!worker.slimDistribution && useWorkerCommand) {
                params.add(0, "worker")
            }

            if (DeployServerUtil.isAkkaSecured(project)) {
                SslUtil.getAkkaSecured(project, DeployServerUtil.getServerWorkingDir(project))?.let { secured ->
                    secured.keys[AkkaSecured.WORKER_KEY_NAME + worker.name]?.let { key ->
                        params.addAll(listOf(
                                "-keyStore",
                                key.keyStoreFile().absolutePath,
                                "-keyStorePassword",
                                key.keyStorePassword,
                                "-trustStore",
                                secured.trustStoreFile().absolutePath,
                                "-trustStorePassword",
                                secured.truststorePassword,
                        ))
                        if (AkkaSecured.KEYSTORE_TYPE != "pkcs12") {
                            params.addAll(listOf(
                                    "-keyPassword",
                                    key.keyPassword,
                            ))
                        }
                    }
                }
            }

            return params
        }


        @JvmStatic
        fun hasRuntimeDirectory(project: Project, worker: Worker): Boolean {
            return getRuntimeDirectory(project, worker) != null
        }

        @JvmStatic
        private fun getRuntimeDirectory(project: Project, worker: Worker): String? {
            return if (worker.runtimeDirectory != null)
                worker.runtimeDirectory
            else
                DeployServerUtil.getServer(project).runtimeDirectory
        }

        @JvmStatic
        private fun getWorkerVersion(project: Project, worker: Worker): String? {
            if (project.hasProperty("deployTaskEngineVersion")) {
                return project.property("deployTaskEngineVersion").toString()
            } else if (!worker.version.isNullOrBlank()) {
                return worker.version
            } else if (!DeployServerUtil.getServer(project).version.isNullOrBlank()) {
                return DeployServerUtil.getServer(project).version.toString()
            } else if (!hasRuntimeDirectory(project, worker)) {
                project.logger.error("Worker Version is not specified")
                exitProcess(1)
            }
            return null
        }

        @JvmStatic
        private fun getDebugPort(project: Project, worker: Worker): Int? {
            return if (PropertyUtil.resolveBooleanValue(project, "debug", true)) {
                PropertyUtil.resolveIntValue(project, "workerDebugPort", worker.debugPort)
            } else {
                null
            }
        }
    }
}
