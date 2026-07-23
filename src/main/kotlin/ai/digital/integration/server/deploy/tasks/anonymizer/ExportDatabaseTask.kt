package ai.digital.integration.server.deploy.tasks.anonymizer

import ai.digital.integration.server.common.domain.Server
import ai.digital.integration.server.deploy.internals.DeployConfigurationsUtil
import ai.digital.integration.server.deploy.internals.DeployServerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.File
import java.nio.file.Paths
import javax.inject.Inject

open class ExportDatabaseTask @Inject constructor(
    private val execOperations: ExecOperations) : DefaultTask() {

    /**
     * Classpath-mode export (server started from the Gradle `deployServer` configuration, i.e.
     * `runtimeDirectory` is set). Unchanged historical behavior — used by classpath-based consumers.
     */
    private fun startFromClasspath(server: Server) {
        val classpath = project.configurations.getByName(DeployConfigurationsUtil.DEPLOY_SERVER)
            .filter { !it.name.endsWith("-sources.jar") }.asPath
        logger.debug("Exporting Database application classpath: \n${classpath}")

        project.logger.lifecycle("[DbUnit][export] (classpath) Starting AnonymizerBootstrapper (workingDir=${server.runtimeDirectory})")

        execOperations.javaexec {
            mainClass.set("com.xebialabs.database.anonymizer.AnonymizerBootstrapper")
            environment["CLASSPATH"] = classpath
            server.runtimeDirectory?.let { dir ->
                workingDir = File(dir)
            }
        }
        project.logger.lifecycle("[DbUnit][export] (classpath) AnonymizerBootstrapper finished; dump written under ${server.runtimeDirectory}")
    }

    /**
     * Dist-mode export (server downloaded & extracted, i.e. `runtimeDirectory == null` — the FE data repos
     * and the backend `xld-integration-server-data`). The Gradle `deployServer` classpath is empty here, so
     * we invoke the server dist's own `bin/db-anonymizer` launcher, which builds the correct classpath from
     * `conf/xld-wrapper.conf.common` and runs `AnonymizerBootstrapper`. It `cd`s to the server home and writes
     * `xl-deploy-repository-dump.xml` (+`.dtd`) there; the consumer's `archiveRepository` renames it to
     * `data.xml`. Must run while the server + central-config are live (the bootstrapper needs the config server).
     */
    private fun exportFromDist(server: Server) {
        val serverHome = DeployServerUtil.getServerWorkingDir(project, server)
        val isWindows = System.getProperty("os.name").lowercase().contains("win")
        val scriptName = if (isWindows) "db-anonymizer.cmd" else "db-anonymizer.sh"
        val script = Paths.get(serverHome, "bin", scriptName).toFile()

        if (!script.exists()) {
            project.logger.warn("[DbUnit][export] (dist) launcher not found at ${script.absolutePath}; skipping export.")
            return
        }

        project.logger.lifecycle("[DbUnit][export] (dist) Running ${scriptName} in ${serverHome} → xl-deploy-repository-dump.xml")
        execOperations.exec {
            workingDir = File(serverHome)
            // Pass the current JVM as JAVA_HOME so the launcher resolves java consistently.
            environment("JAVA_HOME", System.getProperty("java.home"))
            if (isWindows) {
                commandLine("cmd", "/c", script.absolutePath)
            } else {
                commandLine("sh", script.absolutePath)
            }
        }
        project.logger.lifecycle("[DbUnit][export] (dist) finished; dump at ${serverHome}/xl-deploy-repository-dump.xml")
    }

    @TaskAction
    fun run() {
        val server = DeployServerUtil.getServer(project)
        project.logger.lifecycle("Exporting database for Deploy server.")

        if (server.runtimeDirectory != null) {
            project.logger.lifecycle("[DbUnit][export] runtimeDirectory=${server.runtimeDirectory} -> classpath-mode export (startFromClasspath)")
            startFromClasspath(server)
        } else {
            project.logger.lifecycle("[DbUnit][export] runtimeDirectory is null -> dist-mode export (exportFromDist) via the server dist's bin/db-anonymizer launcher")
            exportFromDist(server)
        }
    }

    companion object {
        const val NAME = "exportDatabase"
    }
}
