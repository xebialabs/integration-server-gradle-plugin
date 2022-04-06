package ai.digital.integration.server.release.tasks.cluster.operator.awsopenshift

import ai.digital.integration.server.common.cluster.operator.AwsOpenshiftOperatorHelper
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.release.tasks.cluster.operator.ReleaseOperatorBasedInstallTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedAwsOpenShiftInstallReleaseClusterTask : ReleaseOperatorBasedInstallTask() {

    companion object {
        const val NAME = "operatorBasedAwsOpenShiftInstallReleaseCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(dependsOnTasks())
    }

    @TaskAction
    fun launch() {
        AwsOpenshiftOperatorHelper(project, ProductName.RELEASE).installCluster()
    }
}
