package ai.digital.integration.server.release.tasks.cluster.operator.gcpgke

import ai.digital.integration.server.common.cluster.operator.GcpGkeOperatorHelper
import ai.digital.integration.server.common.cluster.setup.GcpGkeHelper
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.release.tasks.cluster.operator.ReleaseOperatorBasedStartTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedGcpGkeStartReleaseClusterTask : ReleaseOperatorBasedStartTask() {

    companion object {
        const val NAME = "operatorBasedGcpGkeStartReleaseCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(dependsOnTasks())
    }

    @TaskAction
    fun launch() {
        GcpGkeHelper(project, ProductName.RELEASE).launchCluster()
        GcpGkeOperatorHelper(project, ProductName.RELEASE).updateOperator()
    }
}
