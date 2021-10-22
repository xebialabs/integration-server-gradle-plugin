package ai.digital.integration.server.deploy.tasks.server

import ai.digital.integration.server.deploy.internals.DeployServerInitializeUtil
import ai.digital.integration.server.deploy.internals.DeployServerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class PrepareServerTask : DefaultTask() {

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
