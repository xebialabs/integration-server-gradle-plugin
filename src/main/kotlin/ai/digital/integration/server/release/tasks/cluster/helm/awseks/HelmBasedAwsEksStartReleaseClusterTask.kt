package ai.digital.integration.server.release.tasks.cluster.helm.awseks

import ai.digital.integration.server.common.cluster.helm.AwsEksHelmHelper
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.release.tasks.cluster.helm.ReleaseHelmBasedStartTask

import org.gradle.api.tasks.TaskAction

open class HelmBasedAwsEksStartReleaseClusterTask : ReleaseHelmBasedStartTask() {

    companion object {
        const val NAME = "helmBasedAwsEksStartReleaseCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(dependsOnTasks())
    }

    @TaskAction
    fun launch() {
        AwsEksHelmHelper(project, ProductName.RELEASE).launchCluster()
        AwsEksHelmHelper(project, ProductName.RELEASE).setupHelmValues()
    }
}
