package ai.digital.integration.server.deploy.tasks

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.deploy.util.DeployConfigurationsUtil.Companion.SERVER_DATA_DIST
import ai.digital.integration.server.deploy.util.DeployExtensionUtil
import ai.digital.integration.server.common.util.IntegrationServerUtil
import org.gradle.api.tasks.Copy

abstract class DownloadAndExtractDbUnitDataDistTask : Copy() {
    companion object {
        const val NAME = "downloadAndExtractDbUnitData"
    }

    init {
        this.group = PLUGIN_GROUP
        val version = DeployExtensionUtil.getExtension(project).xldIsDataVersion
        if (version != null) {
            project.buildscript.dependencies.add(
                SERVER_DATA_DIST,
                "com.xebialabs.deployit.plugins:xld-is-data:${version}:repository@zip"
            )
            this.from(project.zipTree(project.buildscript.configurations.getByName(SERVER_DATA_DIST).singleFile))
            this.into(IntegrationServerUtil.getDist(project))
        }
    }
}
