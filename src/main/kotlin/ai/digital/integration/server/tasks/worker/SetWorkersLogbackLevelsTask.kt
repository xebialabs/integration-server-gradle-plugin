package ai.digital.integration.server.tasks.worker

import ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.util.WorkerUtil
import org.gradle.api.DefaultTask

abstract class SetWorkersLogbackLevelsTask : DefaultTask() {

    init {
        val slimMustRunAfter =
            if (WorkerUtil.hasSlimWorkers(project)) arrayOf(CopyIntegrationServerTask.NAME) else arrayOf()

        val nonSlimMustRunAfter = if (WorkerUtil.hasNonSlimWorkers(project))
            arrayOf(DownloadAndExtractWorkerDistTask.NAME, SyncServerPluginsWithWorkerTask.NAME) else arrayOf()

        this.group = PLUGIN_GROUP
        this.mustRunAfter(*(slimMustRunAfter + nonSlimMustRunAfter))
        this.onlyIf {
            WorkerUtil.hasWorkers(project)
        }
    }

    companion object {
        @JvmStatic
        val NAME = "setWorkerLogbackLevels"
    }
}
