package ai.digital.integration.server.deploy.tasks

import ai.digital.integration.server.deploy.util.DeployServerInitializeUtil
import ai.digital.integration.server.deploy.util.DeployServerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class PrepareDeployTask : DefaultTask() {

    @TaskAction
    fun launch() {
        DeployServerUtil.getServers(project)
                .forEach { server ->
                    DeployServerInitializeUtil.prepare(project, server)
                }
    }

    companion object {
        const val NAME = "prepareDeploy"
    }
}
