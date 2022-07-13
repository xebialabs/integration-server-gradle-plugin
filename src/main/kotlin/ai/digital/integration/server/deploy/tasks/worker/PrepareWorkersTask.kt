package ai.digital.integration.server.deploy.tasks.worker

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.util.CentralConfigurationServerUtil
import ai.digital.integration.server.common.util.HTTPUtil
import ai.digital.integration.server.deploy.domain.Worker
import ai.digital.integration.server.deploy.internals.WorkerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class PrepareWorkersTask: DefaultTask() {
    companion object {
        const val NAME = "prepareWorkersTask"
    }

    init {
        this.group = PluginConstant.PLUGIN_GROUP
        this.onlyIf {
            WorkerUtil.hasWorkers(project)
        }
        this.onlyIf {
            CentralConfigurationServerUtil.hasCentralConfigurationServer(project)
        }
    }

    @TaskAction
    fun prepare() {
        WorkerUtil.getWorkers(project)
                .forEach { worker ->
                    createConfFile(worker)
                }
    }

    private fun createConfFile(worker: Worker) {
        project.logger.lifecycle("Creating deployit.conf file for ${worker.name}")

        val file = project.file("${WorkerUtil.getWorkerWorkingDir(project, worker)}/conf/deployit.conf")
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }
        file.createNewFile()
        file.appendText("http.port=${HTTPUtil.findFreePort()}\n")
        file.appendText("xl.spring.cloud.uri=${CentralConfigurationServerUtil.getBaseUrl(project)}/centralConfiguration/\n")
        file.appendText("xl.spring.cloud.external-config=true\n")
    }

}