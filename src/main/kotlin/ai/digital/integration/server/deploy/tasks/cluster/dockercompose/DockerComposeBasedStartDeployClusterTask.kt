package ai.digital.integration.server.deploy.tasks.cluster.dockercompose

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.deploy.internals.cluster.DeployDockerClusterHelper
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import javax.inject.Inject

open class DockerComposeBasedStartDeployClusterTask @Inject constructor(
    private val execOperations: ExecOperations) : DefaultTask() {

    companion object {
        const val NAME = "dockerComposeBasedStartDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
    }

    @TaskAction
    fun launch() {
        DeployDockerClusterHelper(execOperations, project).launchCluster()
    }
}
