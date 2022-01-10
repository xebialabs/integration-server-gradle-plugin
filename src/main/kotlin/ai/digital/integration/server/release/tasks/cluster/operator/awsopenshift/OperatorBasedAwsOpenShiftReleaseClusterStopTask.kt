package ai.digital.integration.server.release.tasks.cluster.operator.awsopenshift

import ai.digital.integration.server.common.cluster.operator.AwsOpenshiftHelper
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedAwsOpenShiftReleaseClusterStopTask : DefaultTask() {

    companion object {
        const val NAME = "operatorBasedAwsOpenShiftReleaseClusterStop"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
    }

    @TaskAction
    fun launch() {
        AwsOpenshiftHelper(project, ProductName.RELEASE).shutdownCluster()
    }
}
