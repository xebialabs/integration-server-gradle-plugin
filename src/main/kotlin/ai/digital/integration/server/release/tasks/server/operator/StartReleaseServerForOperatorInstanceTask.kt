package ai.digital.integration.server.release.tasks.server.operator

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.tasks.database.DatabaseStartTask
import ai.digital.integration.server.common.tasks.database.PrepareDatabaseTask
import ai.digital.integration.server.common.util.DbUtil
import ai.digital.integration.server.common.util.DockerComposeUtil
import ai.digital.integration.server.deploy.tasks.server.*
import ai.digital.integration.server.release.util.ReleaseServerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.closureOf

open class StartReleaseServerForOperatorInstanceTask : DefaultTask() {
    companion object {
        const val NAME = "startReleaseServerForOperatorInstance"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP

        val dependencies = mutableListOf(
            ApplicationConfigurationOverrideTask.NAME,
            CopyServerFoldersTask.NAME,
            CopyServerBuildArtifactsTask.NAME,
            ServerCopyOverlaysTask.NAME, if (DbUtil.isDerby(project)) "derbyStart" else DatabaseStartTask.NAME,
            PrepareDatabaseTask.NAME,
            PrepareServerTask.NAME,
            SetServerLogbackLevelsTask.NAME,
            ServerYamlPatchTask.NAME
        )

        this.configure(closureOf<StartReleaseServerForOperatorInstanceTask> {
            dependsOn(dependencies)
        })
    }

    private fun start() {
        ReleaseServerUtil.runDockerBasedInstance(project)
    }

    private fun allowToWriteMountedHostFolders() {
        ReleaseServerUtil.grantPermissionsToIntegrationServerFolder(project)
    }

    @TaskAction
    fun launch() {
        // we only need one server for deployment on the operators
        val server = ReleaseServerUtil.getServer(project)
        if (!server.previousInstallation) {
            project.logger.lifecycle("About to launch Release Server ${server.name} on port " + server.httpPort.toString() + ".")
            allowToWriteMountedHostFolders()
            start()
            ReleaseServerUtil.waitForBoot(project, null)

            val dockerComposeFile = ReleaseServerUtil.getResolvedDockerFile(project).toFile()
            DockerComposeUtil.allowToCleanMountedFiles(project, server, dockerComposeFile)
        }
    }
}
