package ai.digital.integration.server.release.tasks.cluster.helm.awsopenshift

import ai.digital.integration.server.common.cluster.helm.AwsOpenshiftHelmHelper
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class HelmBasedAwsOpenShiftStopReleaseClusterTask : DefaultTask() {

    companion object {
        const val NAME = "helmBasedAwsOpenShiftStopReleaseCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
    }

    @TaskAction
    fun launch() {
        AwsOpenshiftHelmHelper(project, ProductName.RELEASE).shutdownCluster()
    }
}
