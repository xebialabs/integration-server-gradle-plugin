package ai.digital.integration.server.deploy.tasks.centralConfiguration

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.common.util.IntegrationServerUtil
import ai.digital.integration.server.deploy.internals.CentralConfigurationServerUtil
import ai.digital.integration.server.deploy.internals.DeployConfigurationsUtil
import org.gradle.api.tasks.Copy

open class DownloadAndExtractCentralConfigurationServerDistTask : Copy() {

    init {
        this.group = PLUGIN_GROUP

        this.onlyIf {
            CentralConfigurationServerUtil.hasCentralConfigurationServer(project)
        }

        val version = CentralConfigurationServerUtil.getCentralConfigurationServer(project).version
        project.logger.lifecycle("Downloading and extracting the central config server ${version}.")
        project.buildscript.dependencies.add(
                DeployConfigurationsUtil.CENTRAL_CONFIG_DIST,
                "ai.digital.config:central-configuration-server:${version}@zip"
        )

        val taskName = "${NAME}Exec"
        this.dependsOn(project.tasks.register(taskName, Copy::class.java) {
            from(project.zipTree(project.buildscript.configurations.getByName(DeployConfigurationsUtil.CENTRAL_CONFIG_DIST).singleFile))
            into(IntegrationServerUtil.getDist(project))
        })
    }

    companion object {
        const val NAME = "downloadAndExtractCentralConfigurationServer"
    }
}
