package ai.digital.integration.server.deploy.tasks.cluster.operator

import ai.digital.integration.server.deploy.tasks.cluster.k8sinstaller.scanning.KubeBenchInstallerAwsEksTask
import ai.digital.integration.server.deploy.tasks.cluster.k8sinstaller.scanning.KubeBenchInstallerTask
import ai.digital.integration.server.deploy.tasks.server.StartDeployServerForOperatorInstanceTask
import org.gradle.api.DefaultTask

abstract class OperatorBasedStartTask : DefaultTask() {

    fun dependsOnTasks(): Array<String> {
        return arrayOf(
           // StartDeployServerForOperatorInstanceTask.NAME,
            CheckingOutDeployKubernetesOperatorTask.NAME,
            KubeBenchInstallerAwsEksTask.NAME
        )
    }
}
