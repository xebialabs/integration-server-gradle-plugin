package ai.digital.integration.server.release.tasks.cluster.operator.awseks

import ai.digital.integration.server.common.cluster.operator.AwsEksOperatorHelper
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedAwsEksStopReleaseClusterTask : DefaultTask() {

    companion object {
        const val NAME = "operatorBasedAwsEksStopReleaseCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
    }

    @TaskAction
    fun launch() {
        AwsEksOperatorHelper(project, ProductName.RELEASE).shutdownCluster()
    }
}
