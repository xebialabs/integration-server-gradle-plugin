package ai.digital.integration.server.release.tasks.cluster.operator.awseks

import ai.digital.integration.server.common.cluster.operator.AwsEksOperatorHelper
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.release.tasks.cluster.operator.ReleaseOperatorBasedInstallTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedAwsEksInstallReleaseClusterTask : ReleaseOperatorBasedInstallTask() {

    companion object {
        const val NAME = "operatorBasedAwsEksInstallReleaseCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(dependsOnTasks())
    }

    @TaskAction
    fun launch() {
        AwsEksOperatorHelper(project, ProductName.RELEASE).installCluster()
    }
}
