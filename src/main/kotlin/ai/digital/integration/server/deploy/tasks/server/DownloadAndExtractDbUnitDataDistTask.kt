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
        val extension = DeployExtensionUtil.getExtension(project)
        val version = extension.xldIsDataVersion
        if (version != null) {
            val coordinate = "${extension.xldIsDataArtifact}:${version}:repository@zip"
            project.logger.lifecycle("[DbUnit][download] Resolving DBUnit dataset artifact: $coordinate")
            project.dependencies.add(SERVER_DATA_DIST, coordinate)
            val destination = IntegrationServerUtil.getDist(project)
            val taskName = "${NAME}Exec"
            this.dependsOn(project.tasks.register(taskName, Copy::class.java) {
                val zipFile = project.configurations.getByName(SERVER_DATA_DIST).singleFile
                project.logger.lifecycle("[DbUnit][download] Extracting '${zipFile.name}' (from $coordinate) into $destination")
                from(project.zipTree(zipFile))
                into(destination)
            })
        } else {
            project.logger.info("[DbUnit][download] xldIsDataVersion not set; skipping DBUnit dataset download.")
        }
    }
}
