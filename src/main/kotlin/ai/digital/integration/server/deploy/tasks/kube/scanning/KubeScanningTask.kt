package ai.digital.integration.server.deploy.tasks.kube.scanning

import ai.digital.integration.server.common.constant.PluginConstant
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class KubeScanningTask : DefaultTask() {

    companion object {
        const val NAME = "kubeScanning"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(KubeAwsScannerTask.NAME)
    }

    @TaskAction
    fun launch() {
        project.logger.lifecycle("Kube Scanning!!")
    }

}
