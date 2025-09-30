package ai.digital.integration.server.common

import ai.digital.integration.server.common.tasks.cluster.operator.CleanUpClusterTask
import ai.digital.integration.server.deploy.tasks.kube.scanning.CheckingOutKubeBenchTask
import ai.digital.integration.server.deploy.tasks.kube.scanning.KubeAwsScannerFinalizerTask
import ai.digital.integration.server.deploy.tasks.kube.scanning.KubeAwsScannerTask
import ai.digital.integration.server.deploy.tasks.kube.scanning.KubeScanningTask
import org.gradle.api.Project

class KubeScannerRegistry {

    companion object {
        fun register(project: Project) {
            project.tasks.register(CheckingOutKubeBenchTask.NAME, CheckingOutKubeBenchTask::class.java)
            project.tasks.register(KubeAwsScannerFinalizerTask.NAME, KubeAwsScannerFinalizerTask::class.java)
            project.tasks.register(KubeAwsScannerTask.NAME, KubeAwsScannerTask::class.java)
            project.tasks.register(KubeScanningTask.NAME, KubeScanningTask::class.java)
            project.tasks.register(CleanUpClusterTask.NAME, CleanUpClusterTask::class.java)
        }
    }
}
