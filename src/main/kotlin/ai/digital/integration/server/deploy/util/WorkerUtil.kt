package ai.digital.integration.server.deploy.util

import ai.digital.integration.server.common.domain.AkkaSecured
import ai.digital.integration.server.common.util.IntegrationServerUtil
import ai.digital.integration.server.common.util.PropertyUtil
import ai.digital.integration.server.common.util.TlsUtil
import ai.digital.integration.server.deploy.domain.Worker
import org.gradle.api.Project
import java.nio.file.Paths
import kotlin.system.exitProcess

class WorkerUtil {
    companion object {
        fun hasWorkers(project: Project): Boolean {
            return DeployExtensionUtil.getExtension(project).workers.size > 0
        }

        fun hasSlimWorkers(project: Project): Boolean {
            return getWorkers(project).any { worker ->
                worker.slimDistribution
            }
        }

        fun hasNonSlimWorkers(project: Project): Boolean {
            return getWorkers(project).any { worker ->
                !worker.slimDistribution
            }
        }

        fun getWorkers(project: Project): List<Worker> {
            return DeployExtensionUtil.getExtension(project).workers.map { worker: Worker ->
                enrichWorker(project, worker)
            }
        }

        private fun enrichWorker(project: Project, worker: Worker): Worker {
            worker.debugPort = getDebugPort(project, worker)
            worker.version = getWorkerVersion(project, worker)
            return worker
        }

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

        fun isExternalRuntimeWorker(project: Project, worker: Worker): Boolean {
            return getRuntimeDirectory(project, worker) == null ||
                    (!getRuntimeDirectory(project, worker).isNullOrEmpty() &&
                            getWorkerWorkingDir(project, worker) != DeployServerUtil.getServerWorkingDir(project))
        }

        fun isDistDownloadRequired(project: Project, worker: Worker): Boolean {
            return getRuntimeDirectory(project, worker) == null
        }

        fun composeProgramParams(
            project: Project,
            worker: Worker,
            hostName: String,
            port: String,
            useWorkerCommand: Boolean,
        ): List<String> {

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
                TlsUtil.getAkkaSecured(project, DeployServerUtil.getServerWorkingDir(project))?.let { secured ->
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


        fun hasRuntimeDirectory(project: Project, worker: Worker): Boolean {
            return getRuntimeDirectory(project, worker) != null
        }

        private fun getRuntimeDirectory(project: Project, worker: Worker): String? {
            return if (worker.runtimeDirectory != null)
                worker.runtimeDirectory
            else
                DeployServerUtil.getServer(project).runtimeDirectory
        }

        private fun getWorkerVersion(project: Project, worker: Worker): String? {
            if (!worker.version.isNullOrBlank()) {
                return worker.version
            } else if (project.hasProperty("deployTaskEngineVersion")) {
                return project.property("deployTaskEngineVersion").toString()
            } else if (!DeployServerUtil.getServer(project).version.isNullOrBlank()) {
                return DeployServerUtil.getServer(project).version.toString()
            } else if (!hasRuntimeDirectory(project, worker)) {
                project.logger.error("Worker Version is not specified")
                exitProcess(1)
            }
            return null
        }

        private fun getDebugPort(project: Project, worker: Worker): Int? {
            return if (PropertyUtil.resolveBooleanValue(project, "debug", true)) {
                PropertyUtil.resolveIntValue(project, "workerDebugPort", worker.debugPort)
            } else {
                null
            }
        }
    }
}
