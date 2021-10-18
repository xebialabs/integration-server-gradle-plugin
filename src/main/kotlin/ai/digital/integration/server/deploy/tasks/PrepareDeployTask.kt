package ai.digital.integration.server.deploy.tasks

import ai.digital.integration.server.deploy.util.DeployServerInitializeUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class PrepareDeployTask : DefaultTask() {

    @TaskAction
    fun launch() {
        DeployServerInitializeUtil.prepare(project)
    }

    companion object {
        const val NAME = "prepareDeploy"
    }
}
