package ai.digital.integration.server.tasks

import ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.util.ConfigurationsUtil.Companion.SERVER_DIST
import ai.digital.integration.server.util.DeployServerUtil
import ai.digital.integration.server.util.IntegrationServerUtil
import org.gradle.api.tasks.Copy

abstract class DownloadAndExtractServerDistTask : Copy() {

    companion object {
        @JvmStatic
        val NAME = "downloadAndExtractServer"
    }

    init {
        this.dependsOn(PrepareDeployTask.NAME)

        val server = DeployServerUtil.getServer(project)
        this.group = PLUGIN_GROUP

        if (DeployServerUtil.isDistDownloadRequired(project)) {
            project.logger.lifecycle("Downloading and extracting the server.")
            project.buildscript.dependencies.add(
                SERVER_DIST,
                "com.xebialabs.deployit:xl-deploy-base:${server.version}:server@zip"
            )
            this.from(project.zipTree(project.buildscript.configurations.getByName(SERVER_DIST).singleFile))
            this.into(IntegrationServerUtil.getDist(project))
        }
    }
}
