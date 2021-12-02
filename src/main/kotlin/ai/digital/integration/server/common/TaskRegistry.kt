package ai.digital.integration.server.common

import ai.digital.integration.server.common.tasks.ShutdownIntegrationServerTask
import ai.digital.integration.server.common.tasks.StartIntegrationServerTask
import ai.digital.integration.server.deploy.tasks.kube.scanning.CheckingOutKubeBenchTask
import ai.digital.integration.server.deploy.tasks.kube.scanning.KubeAwsScannerFinalizerTask
import ai.digital.integration.server.deploy.tasks.kube.scanning.KubeAwsScannerTask
import ai.digital.integration.server.deploy.tasks.kube.scanning.KubeScanningTask
import org.gradle.api.Project

class TaskRegistry {

    companion object {
        fun register(project: Project) {
            project.tasks.create(StartIntegrationServerTask.NAME, StartIntegrationServerTask::class.java)
            project.tasks.create(ShutdownIntegrationServerTask.NAME, ShutdownIntegrationServerTask::class.java)
            project.tasks.create(KubeScanningTask.NAME, KubeScanningTask::class.java)
            project.tasks.create(CheckingOutKubeBenchTask.NAME, CheckingOutKubeBenchTask::class.java)
            project.tasks.create(KubeAwsScannerTask.NAME, KubeAwsScannerTask::class.java)
            project.tasks.create(KubeAwsScannerFinalizerTask.NAME, KubeAwsScannerFinalizerTask::class.java)
        }
    }
}
