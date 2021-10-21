package ai.digital.integration.server.deploy.tasks.server

import ai.digital.integration.server.deploy.internals.DeployServerInitializeUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class PrepareServerTask : DefaultTask() {

    @TaskAction
    fun launch() {
        DeployServerInitializeUtil.prepare(project)
    }

    companion object {
        const val NAME = "prepareDeploy"
    }
}
