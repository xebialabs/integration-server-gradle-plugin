package ai.digital.integration.server.release.tasks.cluster.operator

import ai.digital.integration.server.deploy.tasks.server.operator.StartDeployServerForOperatorInstanceTask
import ai.digital.integration.server.release.tasks.server.operator.StartReleaseToGetLicenceTask
import org.gradle.api.DefaultTask

abstract class ReleaseOperatorBasedStartTask : DefaultTask() {

    fun dependsOnTasks(): Array<String> {
        return arrayOf(
            CheckingOutReleaseKubernetesOperatorTask.NAME,
            StartDeployServerForOperatorInstanceTask.NAME,
            StartReleaseToGetLicenceTask.NAME
        )
    }
}
