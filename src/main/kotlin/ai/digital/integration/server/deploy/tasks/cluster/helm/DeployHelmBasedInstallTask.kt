package ai.digital.integration.server.deploy.tasks.cluster.helm

import ai.digital.integration.server.deploy.tasks.cluster.operator.OperatorBasedStartDeployClusterTask
import org.gradle.api.DefaultTask

abstract class DeployHelmBasedInstallTask : DefaultTask() {

    fun dependsOnTasks(): Array<String> {
        return arrayOf(
                HelmBasedStartDeployClusterTask.NAME
        )
    }
}
