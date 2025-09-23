package ai.digital.integration.server.deploy.tasks.server

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.common.util.IntegrationServerUtil
import ai.digital.integration.server.deploy.internals.DeployConfigurationsUtil.Companion.SERVER_DIST
import ai.digital.integration.server.deploy.internals.DeployServerUtil
import org.gradle.api.tasks.Copy

abstract class DownloadAndExtractServerDistTask : Copy() {

    companion object {
        @JvmStatic
        val NAME = "downloadAndExtractServer"
    }

    init {
        this.dependsOn(PrepareServerTask.NAME)
        this.group = PLUGIN_GROUP

        DeployServerUtil.getServers(project)
            .forEach { server ->
                if (DeployServerUtil.isDistDownloadRequired(project, server)) {
                    project.logger.lifecycle("Downloading and extracting the server ${server.name}")
                    val distName = "$SERVER_DIST$server.name"
                    project.configurations.create(distName)

                    project.dependencies.add(
                        distName,
                        "com.xebialabs.deployit:xl-deploy-base:${server.version}:server@zip"
                    )

                    val taskName = "$NAME${server.name}"
                    this.dependsOn(project.tasks.register(taskName, Copy::class.java) {
                        from(project.zipTree(project.configurations.getByName(distName).singleFile))
                        into(IntegrationServerUtil.getDist(project))
                    })
                }
            }
    }
}
