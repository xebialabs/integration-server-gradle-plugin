package ai.digital.integration.server.deploy.tasks.server

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.deploy.internals.DeployConfigurationsUtil.Companion.SERVER_DATA_DIST
import ai.digital.integration.server.deploy.internals.DeployExtensionUtil
import ai.digital.integration.server.common.util.IntegrationServerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Copy

open class DownloadAndExtractDbUnitDataDistTask : DefaultTask() {
    companion object {
        const val NAME = "downloadAndExtractDbUnitData"
    }

    init {
        this.group = PLUGIN_GROUP
        val version = DeployExtensionUtil.getExtension(project).xldIsDataVersion
        if (version != null) {
            project.dependencies.add(
                SERVER_DATA_DIST,
                "com.xebialabs.deployit.plugins:xld-is-data:${version}:repository@zip"
            )
            val taskName = "${NAME}Exec"
            this.dependsOn(project.tasks.register(taskName, Copy::class.java) {
                from(project.zipTree(project.configurations.getByName(SERVER_DATA_DIST).singleFile))
                into(IntegrationServerUtil.getDist(project))
            })

        }
    }
}
