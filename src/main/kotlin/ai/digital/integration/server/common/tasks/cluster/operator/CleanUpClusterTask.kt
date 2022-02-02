package ai.digital.integration.server.common.tasks.cluster.operator

import ai.digital.integration.server.common.cluster.operator.OperatorHelper
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.release.util.ReleaseServerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import java.time.Duration

/**
 * Clean up all resources on cluster, it uses existing default kubectl connection settings.
 */
open class CleanUpClusterTask : DefaultTask() {

    companion object {
        const val NAME = "cleanUpCluster"
    }

    @Input
    val cleanUpWaitTimeout = project.objects.property<Duration>().value(Duration.ofMinutes(2))

    @TaskAction
    fun launch() {
        val productName = if (ReleaseServerUtil.isReleaseServerDefined(project)) {
            ProductName.RELEASE
        } else {
            ProductName.DEPLOY
        }

        val operatorHelper = OperatorHelper.getOperatorHelper(project, productName)
        operatorHelper.cleanUpCluster(cleanUpWaitTimeout.get())
    }
}
