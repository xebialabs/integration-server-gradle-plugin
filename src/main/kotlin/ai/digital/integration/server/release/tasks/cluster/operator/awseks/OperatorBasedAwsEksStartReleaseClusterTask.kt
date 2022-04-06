package ai.digital.integration.server.release.tasks.cluster.operator.awseks

import ai.digital.integration.server.common.cluster.setup.AwsEksHelper
import ai.digital.integration.server.common.cluster.operator.AwsEksOperatorHelper
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.release.tasks.cluster.operator.ReleaseOperatorBasedStartTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedAwsEksStartReleaseClusterTask : ReleaseOperatorBasedStartTask() {

    companion object {
        const val NAME = "operatorBasedAwsEksStartReleaseCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(dependsOnTasks())
    }

    @TaskAction
    fun launch() {
        AwsEksHelper(project, ProductName.RELEASE).launchCluster()
        AwsEksOperatorHelper(project, ProductName.RELEASE).updateOperator()
    }
}
