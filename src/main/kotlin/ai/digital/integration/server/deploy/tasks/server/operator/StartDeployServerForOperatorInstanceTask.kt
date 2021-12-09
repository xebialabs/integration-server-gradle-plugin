package ai.digital.integration.server.deploy.tasks.server.operator

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.tasks.database.DatabaseStartTask
import ai.digital.integration.server.common.tasks.database.PrepareDatabaseTask
import ai.digital.integration.server.common.util.DbUtil
import ai.digital.integration.server.deploy.internals.DeployServerUtil
import ai.digital.integration.server.deploy.tasks.server.*
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.closureOf

open class StartDeployServerForOperatorInstanceTask : DefaultTask() {
    companion object {
        const val NAME = "startDeployServerForOperatorInstance"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP

        val dependencies = mutableListOf(
            ApplicationConfigurationOverrideTask.NAME,
            CopyServerFoldersTask.NAME,
            CopyServerBuildArtifactsTask.NAME,
            ServerCopyOverlaysTask.NAME, if (DbUtil.isDerby(project)) "derbyStart" else DatabaseStartTask.NAME,
            OperatorCentralConfigurationTask.NAME,
            PrepareDatabaseTask.NAME,
            PrepareServerTask.NAME,
            SetServerLogbackLevelsTask.NAME,
            ServerYamlPatchTask.NAME
        )

        this.configure(closureOf<StartDeployServerForOperatorInstanceTask> {
            dependsOn(dependencies)
        })
    }

    private fun start() {
        project.exec {
            executable = "docker-compose"
            args = listOf("-f", DeployServerUtil.getResolvedDockerFile(project).toFile().toString(), "up", "-d")
        }
    }

    private fun allowToWriteMountedHostFolders() {
        DeployServerUtil.grantPermissionsToIntegrationServerFolder(project)
    }

    @TaskAction
    fun launch() {
        DeployServerUtil.getServers(project)
            .filter { server -> !server.previousInstallation }
            .forEach { server ->
                project.logger.lifecycle("About to launch Deploy Server ${server.name} on port " + server.httpPort.toString() + ".")
                allowToWriteMountedHostFolders()
                start()
                DeployServerUtil.waitForBoot(project, null, auxiliaryServer = true)
            }
    }
}
