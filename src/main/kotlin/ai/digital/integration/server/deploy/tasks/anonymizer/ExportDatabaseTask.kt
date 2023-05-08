package ai.digital.integration.server.deploy.tasks.anonymizer

import ai.digital.integration.server.common.domain.Server
import ai.digital.integration.server.deploy.internals.DeployConfigurationsUtil
import ai.digital.integration.server.deploy.internals.DeployServerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

open class ExportDatabaseTask : DefaultTask() {

    private fun startFromClasspath(server: Server) {
        val classpath = project.configurations.getByName(DeployConfigurationsUtil.DEPLOY_SERVER)
            .filter { !it.name.endsWith("-sources.jar") }.asPath
        logger.debug("Exporting Database application classpath: \n${classpath}")

        project.logger.lifecycle("Starting to export the database ")

        project.javaexec {
            mainClass.set("com.xebialabs.database.anonymizer.AnonymizerBootstrapper")
            environment["CLASSPATH"] = classpath
            server.runtimeDirectory?.let { dir ->
                workingDir = File(dir)
            }
        }
    }

    @TaskAction
    fun run() {
        val server = DeployServerUtil.getServer(project)
        project.logger.lifecycle("Exporting database for Deploy server.")

        if (server.runtimeDirectory != null) {
            startFromClasspath(server)
        }
    }

    companion object {
        const val NAME = "exportDatabase"
    }
}
