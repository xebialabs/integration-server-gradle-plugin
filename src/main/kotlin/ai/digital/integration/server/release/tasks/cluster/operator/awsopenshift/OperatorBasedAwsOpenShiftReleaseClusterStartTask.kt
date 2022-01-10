package ai.digital.integration.server.release.tasks.cluster.operator.awsopenshift

import ai.digital.integration.server.common.cluster.operator.AwsOpenshiftHelper
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.release.tasks.cluster.operator.ReleaseOperatorBasedStartTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedAwsOpenShiftReleaseClusterStartTask : ReleaseOperatorBasedStartTask() {

    companion object {
        const val NAME = "operatorBasedAwsOpenShiftStartReleaseCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(dependsOnTasks())
    }

    @TaskAction
    fun launch() {
        AwsOpenshiftHelper(project, ProductName.RELEASE).launchCluster()
    }
}
