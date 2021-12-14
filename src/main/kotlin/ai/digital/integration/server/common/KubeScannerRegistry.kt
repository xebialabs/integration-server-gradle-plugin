package ai.digital.integration.server.common

import ai.digital.integration.server.deploy.tasks.kube.scanning.CheckingOutKubeBenchTask
import ai.digital.integration.server.deploy.tasks.kube.scanning.KubeAwsScannerFinalizerTask
import ai.digital.integration.server.deploy.tasks.kube.scanning.KubeAwsScannerTask
import ai.digital.integration.server.deploy.tasks.kube.scanning.KubeScanningTask
import org.gradle.api.Project

class KubeScannerRegistry {

    companion object {
        fun register(project: Project) {
            project.tasks.create(CheckingOutKubeBenchTask.NAME, CheckingOutKubeBenchTask::class.java)
            project.tasks.create(KubeAwsScannerFinalizerTask.NAME, KubeAwsScannerFinalizerTask::class.java)
            project.tasks.create(KubeAwsScannerTask.NAME, KubeAwsScannerTask::class.java)
            project.tasks.create(KubeScanningTask.NAME, KubeScanningTask::class.java)
        }
    }
}
