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

        /**
         * Single source of truth for the gating rule shared by download + import: the new dist-mode extraction
         * / import behavior applies ONLY when a consumer OVERRIDES the DBUnit coordinate to something other than
         * the backend default (xld-is-data). The default keeps the exact legacy behavior. Accepts a "group:name"
         * (or bare "name") coordinate and compares the trailing artifact name.
         */
        fun isCustomDataArtifact(artifactCoordinate: String): Boolean =
            artifactCoordinate.substringAfterLast(":") != DEFAULT_DATA_ARTIFACT_NAME

        /** The bare artifact name from a "group:name" (or bare "name") coordinate. */
        fun dataArtifactName(artifactCoordinate: String): String =
            artifactCoordinate.substringAfterLast(":")

        /**
         * Single source of truth for the extracted repository folder name. This task extracts the DBUnit
         * dataset zip INTO this folder and ImportDbUnitDataTask reads `<folder>/data.xml` FROM it, so both
         * MUST derive it identically — otherwise the import silently looks in the wrong place and fails with
         * FileNotFoundException. Keep this the only place the layout is computed.
         */
        fun repositoryFolderName(artifactCoordinate: String, version: String): String =
            "${dataArtifactName(artifactCoordinate)}-${version}-repository"
    }

    init {
        this.group = PLUGIN_GROUP
        val extension = DeployExtensionUtil.getExtension(project)
        val version = extension.xldIsDataVersion
        if (version != null) {
            val coordinate = "${extension.xldIsDataArtifact}:${version}:repository@zip"
            project.logger.lifecycle("[DbUnit][download] Resolving DBUnit dataset artifact: $coordinate")
            project.dependencies.add(SERVER_DATA_DIST, coordinate)
            // Only FE consumers that OVERRIDE the coordinate (e.g. xld-ci-explorer-data) get the new extraction;
            // the backend default (xld-is-data) keeps the exact legacy behavior so it is entirely unaffected.
            val isCustomDataArtifact = isCustomDataArtifact(extension.xldIsDataArtifact)
            val taskName = "${NAME}Exec"
            if (isCustomDataArtifact) {
                // Extract into the dedicated <artifact>-<version>-repository/ subfolder that ImportDbUnitDataTask
                // reads data.xml from (the zip carries data.xml at its root). Scoping the destination to this
                // subfolder (instead of the whole build/integration-server) also avoids Gradle's implicit-dependency
                // validation error with tasks like databaseStart that share the dist dir. The prefix-strip keeps it
                // correct whether the zip has data.xml at the root or nested under the repository folder.
                val repoFolder = repositoryFolderName(extension.xldIsDataArtifact, version)
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
