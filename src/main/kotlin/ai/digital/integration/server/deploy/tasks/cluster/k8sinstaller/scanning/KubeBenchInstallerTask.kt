package ai.digital.integration.server.deploy.tasks.cluster.k8sinstaller.scanning

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.util.FileUtil
import ai.digital.integration.server.common.util.IntegrationServerUtil
import ai.digital.integration.server.common.util.ProcessUtil
import ai.digital.integration.server.common.util.YamlFileUtil
import ai.digital.integration.server.deploy.internals.KubeBenchUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.Paths

open class KubeBenchInstallerTask : DefaultTask() {

    companion object {
        const val NAME = "kubeBenchInstallerTask"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(CheckingOutKubeBenchTask.NAME)
    }

    @TaskAction
    fun launch() {
        project.logger.lifecycle("cluster setup kube-bench test")


    }

}
