package ai.digital.integration.server.deploy.tasks.cluster.xlblueprint

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.util.FileUtil
import ai.digital.integration.server.deploy.internals.CliUtil
import ai.digital.integration.server.deploy.tasks.cli.DownloadAndExtractCliDistTask
import ai.digital.integration.server.deploy.tasks.server.operator.StopDeployServerForOperatorInstanceTask
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.IOException
import java.nio.file.Paths

open class XlBlueprintBasedStopDeployClusterTask : DefaultTask() {

    companion object {
        const val NAME = "xlBlueprintBasedStopDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(DownloadAndExtractCliDistTask.NAME)
        this.finalizedBy(StopDeployServerForOperatorInstanceTask.NAME)
    }

    @TaskAction
    fun launch() {
        val fileStream = {}::class.java.classLoader.getResourceAsStream("xl-blueprint/python/undeploy.py")

        val resultComposeFilePath = Paths.get(project.buildDir.toPath().resolve("xlBlueprint-work").toAbsolutePath().toString(), "undeploy.py")
        fileStream?.let {
            FileUtil.copyFile(it, resultComposeFilePath)
        }
        return try {
            CliUtil.executeScripts(project,
                    listOf(resultComposeFilePath.toFile()),
                    "undeploy.py",
                    auxiliaryServer = true)

        } catch (e: RuntimeException) {
            project.logger.error("Undeploy didn't run. Check if operator's deploy server is running on port 4516: ${e.message}")

        } catch (e: IOException) {
            project.logger.error("Undeploy didn't run. Check if operator's deploy server has all files: ${e.message}")

        }
    }
}
