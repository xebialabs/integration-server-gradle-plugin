package ai.digital.integration.server.release.tasks.cluster.operator.awsopenshift

import ai.digital.integration.server.common.cluster.operator.AwsOpenshiftOperatorHelper
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedAwsOpenShiftStopReleaseClusterTask : DefaultTask() {

    companion object {
        const val NAME = "operatorBasedAwsOpenShiftStopReleaseCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
    }

    @TaskAction
    fun launch() {
        AwsOpenshiftOperatorHelper(project, ProductName.RELEASE).shutdownCluster()
    }
}
