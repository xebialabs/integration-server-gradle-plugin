package ai.digital.integration.server.deploy.tasks.cluster.operator

import ai.digital.integration.server.deploy.tasks.server.operator.StopDeployServerForOperatorInstanceTask
import org.gradle.api.DefaultTask

abstract class OperatorBasedStopTask : DefaultTask() {

    fun finalizedBy(): Array<String> {
        return arrayOf(
            StopDeployServerForOperatorInstanceTask.NAME
        )
    }
}
