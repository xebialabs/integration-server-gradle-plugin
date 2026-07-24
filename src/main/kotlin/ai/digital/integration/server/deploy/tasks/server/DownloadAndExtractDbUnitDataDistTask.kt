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
            // Extract into the dedicated <artifact>-<version>-repository/ subfolder that ImportDbUnitDataTask
            // reads data.xml from. The repository zip carries data.xml at its root, so extracting into this
            // subfolder yields <dist>/<artifact>-<version>-repository/data.xml. Scoping the destination to this
            // subfolder (instead of the whole build/integration-server) also avoids Gradle's implicit-dependency
            // validation error with tasks like databaseStart that share the dist dir.
            val artifactName = extension.xldIsDataArtifact.substringAfterLast(":")
            val destination = "${IntegrationServerUtil.getDist(project)}/${artifactName}-${version}-repository"
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
