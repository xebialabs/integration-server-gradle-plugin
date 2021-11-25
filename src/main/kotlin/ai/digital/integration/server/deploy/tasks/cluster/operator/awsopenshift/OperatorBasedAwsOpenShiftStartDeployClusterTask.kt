package ai.digital.integration.server.deploy.tasks.cluster.operator.awsopenshift

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.deploy.internals.cluster.operator.AwsOpenshiftHelper
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedAwsOpenShiftStartDeployClusterTask : DefaultTask() {

    companion object {
        const val NAME = "operatorBasedAwsOpenShiftStartDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
    }

    @TaskAction
    fun launch() {
        AwsOpenshiftHelper(project).launchCluster()
    }
}
