package ai.digital.integration.server.release.tasks.cluster.operator.awseks

import ai.digital.integration.server.common.cluster.operator.AwsEksHelper
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.release.tasks.cluster.operator.ReleaseOperatorBasedStopTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedAwsEksReleaseClusterStopTask : ReleaseOperatorBasedStopTask() {

    companion object {
        const val NAME = "operatorBasedAwsEksStopReleaseCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(dependsOnTasks())
    }

    @TaskAction
    fun launch() {
        AwsEksHelper(project, ProductName.RELEASE).shutdownCluster()
    }
}
