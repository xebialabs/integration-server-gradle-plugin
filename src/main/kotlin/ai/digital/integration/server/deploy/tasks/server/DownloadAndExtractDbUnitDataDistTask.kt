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
        // The backend default DBUnit artifact name. When a consumer overrides the coordinate to something else
        // (e.g. FE's xld-ci-explorer-data), the new dist-mode extraction/import path is used; the default keeps
        // the legacy behavior untouched so xld-integration-server-data / xld-deploy backend are unaffected.
        const val DEFAULT_DATA_ARTIFACT_NAME = "xld-is-data"
    }

    init {
        this.group = PLUGIN_GROUP
        val extension = DeployExtensionUtil.getExtension(project)
        val version = extension.xldIsDataVersion
        if (version != null) {
            val coordinate = "${extension.xldIsDataArtifact}:${version}:repository@zip"
            project.logger.lifecycle("[DbUnit][download] Resolving DBUnit dataset artifact: $coordinate")
            project.dependencies.add(SERVER_DATA_DIST, coordinate)
            val artifactName = extension.xldIsDataArtifact.substringAfterLast(":")
            // Only FE consumers that OVERRIDE the coordinate (e.g. xld-ci-explorer-data) get the new extraction;
            // the backend default (xld-is-data) keeps the exact legacy behavior so it is entirely unaffected.
            val isCustomDataArtifact = artifactName != DEFAULT_DATA_ARTIFACT_NAME
            val taskName = "${NAME}Exec"
            if (isCustomDataArtifact) {
                // Extract into the dedicated <artifact>-<version>-repository/ subfolder that ImportDbUnitDataTask
                // reads data.xml from (the zip carries data.xml at its root). Scoping the destination to this
                // subfolder (instead of the whole build/integration-server) also avoids Gradle's implicit-dependency
                // validation error with tasks like databaseStart that share the dist dir. The prefix-strip keeps it
                // correct whether the zip has data.xml at the root or nested under the repository folder.
                val repoFolder = "${artifactName}-${version}-repository"
                val destination = "${IntegrationServerUtil.getDist(project)}/${repoFolder}"
                this.dependsOn(project.tasks.register(taskName, Copy::class.java) {
                    val zipFile = project.configurations.getByName(SERVER_DATA_DIST).singleFile
                    project.logger.lifecycle("[DbUnit][download] Extracting '${zipFile.name}' (from $coordinate) into $destination")
                    from(project.zipTree(zipFile)) {
                        eachFile {
                            val prefix = "${repoFolder}/"
                            if (path.startsWith(prefix)) {
                                path = path.removePrefix(prefix)
                            }
                        }
                        includeEmptyDirs = false
                    }
                    into(destination)
                })
            } else {
                // Legacy behavior for the backend's xld-is-data — unchanged.
                val destination = IntegrationServerUtil.getDist(project)
                this.dependsOn(project.tasks.register(taskName, Copy::class.java) {
                    val zipFile = project.configurations.getByName(SERVER_DATA_DIST).singleFile
                    project.logger.lifecycle("[DbUnit][download] Extracting '${zipFile.name}' (from $coordinate) into $destination")
                    from(project.zipTree(zipFile))
                    into(destination)
                })
            }
        } else {
            project.logger.info("[DbUnit][download] xldIsDataVersion not set; skipping DBUnit dataset download.")
        }
    }
}
