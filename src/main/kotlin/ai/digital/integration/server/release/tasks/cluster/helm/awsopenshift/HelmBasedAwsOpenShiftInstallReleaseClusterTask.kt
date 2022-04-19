package ai.digital.integration.server.release.tasks.cluster.helm.awsopenshift

import ai.digital.integration.server.common.cluster.helm.AwsOpenshiftHelmHelper
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.release.tasks.cluster.helm.ReleaseHelmBasedInstallTask
import org.gradle.api.tasks.TaskAction

open class HelmBasedAwsOpenShiftInstallReleaseClusterTask : ReleaseHelmBasedInstallTask() {

    companion object {
        const val NAME = "helmBasedAwsOpenShiftInstallReleaseCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(dependsOnTasks())
    }

    @TaskAction
    fun launch() {
        AwsOpenshiftHelmHelper(project, ProductName.RELEASE).helmInstallCluster()
    }
}
