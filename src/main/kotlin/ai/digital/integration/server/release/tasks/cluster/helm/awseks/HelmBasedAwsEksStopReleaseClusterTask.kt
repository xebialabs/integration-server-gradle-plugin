package ai.digital.integration.server.release.tasks.cluster.helm.awseks

import ai.digital.integration.server.common.cluster.helm.AwsEksHelmHelper
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class HelmBasedAwsEksStopReleaseClusterTask : DefaultTask() {

    companion object {
        const val NAME = "helmBasedAwsEksStopReleaseCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
    }

    @TaskAction
    fun launch() {
        AwsEksHelmHelper(project, ProductName.RELEASE).shutdownCluster()
    }
}
