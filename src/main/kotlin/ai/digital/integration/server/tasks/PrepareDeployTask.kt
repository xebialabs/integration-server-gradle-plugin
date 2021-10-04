package ai.digital.integration.server.tasks

import ai.digital.integration.server.util.ServerInitializeUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class PrepareDeployTask : DefaultTask() {

    @TaskAction
    fun launch() {
        ServerInitializeUtil.prepare(project)
    }

    companion object {
        @JvmStatic
        val NAME = "prepareDeploy"
    }
}
