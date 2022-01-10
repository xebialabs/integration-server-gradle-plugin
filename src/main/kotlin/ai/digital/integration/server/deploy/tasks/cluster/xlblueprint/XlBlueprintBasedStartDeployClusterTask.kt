package ai.digital.integration.server.deploy.tasks.cluster.xlblueprint

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.deploy.internals.cluster.DeployDockerClusterHelper
import ai.digital.integration.server.deploy.tasks.server.operator.StartDeployServerForOperatorInstanceTask
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class XlBlueprintBasedStartDeployClusterTask : DefaultTask() {

    companion object {
        const val NAME = "xlBlueprintBasedStartDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(StartDeployServerForOperatorInstanceTask.NAME)
    }

    @TaskAction
    fun launch() {
        project.logger.lifecycle("Xl Blueprint based Deploy Cluster has started.")
    }
}
