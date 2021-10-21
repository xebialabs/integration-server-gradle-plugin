package ai.digital.integration.server.deploy.tasks.server

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.deploy.internals.DeployConfigurationsUtil.Companion.SERVER_DIST
import ai.digital.integration.server.deploy.internals.DeployServerUtil
import ai.digital.integration.server.common.util.IntegrationServerUtil
import org.gradle.api.tasks.Copy

open class DownloadAndExtractServerDistTask : Copy() {

    companion object {
        const val NAME = "downloadAndExtractServer"
    }

    init {
        this.dependsOn(PrepareServerTask.NAME)

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
