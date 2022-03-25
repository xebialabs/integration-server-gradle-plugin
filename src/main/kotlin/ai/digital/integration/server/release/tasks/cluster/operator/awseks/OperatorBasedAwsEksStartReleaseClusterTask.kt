package ai.digital.integration.server.release.tasks.cluster.operator.awseks

import ai.digital.integration.server.common.cluster.setup.AwsEks
import ai.digital.integration.server.common.cluster.operator.AwsEksHelper
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
        AwsEks(project, ProductName.RELEASE).launchCluster()
        AwsEksHelper(project, ProductName.RELEASE).updateOperator()
    }
}
