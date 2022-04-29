package ai.digital.integration.server.release.tasks.cluster.helm.awseks

import ai.digital.integration.server.common.cluster.helm.AwsEksHelmHelper
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.release.tasks.cluster.helm.ReleaseHelmBasedInstallTask
import org.gradle.api.tasks.TaskAction

open class HelmBasedAwsEksInstallReleaseClusterTask : ReleaseHelmBasedInstallTask() {

    companion object {
        const val NAME = "helmBasedAwsEksInstallReleaseCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(dependsOnTasks())
    }

    @TaskAction
    fun launch() {
        AwsEksHelmHelper(project, ProductName.RELEASE).helmInstallCluster()
    }
}
