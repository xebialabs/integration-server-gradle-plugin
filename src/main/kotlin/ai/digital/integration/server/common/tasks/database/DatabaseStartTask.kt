package ai.digital.integration.server.common.tasks.database

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.common.util.DbUtil
import ai.digital.integration.server.common.util.FileUtil
import ai.digital.integration.server.common.util.IntegrationServerUtil
import ai.digital.integration.server.common.util.PropertyUtil
import ai.digital.integration.server.deploy.tasks.centralConfiguration.DownloadAndExtractCentralConfigurationServerDistTask
import ai.digital.integration.server.deploy.tasks.cli.DownloadAndExtractCliDistTask
import ai.digital.integration.server.deploy.tasks.server.ApplicationConfigurationOverrideTask
import com.palantir.gradle.docker.DockerComposeUp
import org.gradle.api.GradleException
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.inject.Inject

abstract class DatabaseStartTask @Inject constructor(
    private val execOperations: ExecOperations) : DockerComposeUp() {

    companion object {
        const val NAME = "databaseStart"
    }

    init {
        this.group = PLUGIN_GROUP
        this.mustRunAfter(ApplicationConfigurationOverrideTask.NAME, DownloadAndExtractCliDistTask.NAME, DownloadAndExtractCentralConfigurationServerDistTask.NAME)
    }

    override fun getDescription(): String {
        return "Starts database instance using `docker-compose` and ${DbUtil.dockerComposeFileName(project)} file."
    }

    @InputFiles
    @Optional
    fun getDockerComposeFileInput(): File? {
        val dbName = DbUtil.databaseName(project)
        // Return null for embedded databases (H2) to skip input file validation
        return if (DbUtil.isEmbeddedDatabase(dbName)) {
            null
        } else {
            getDockerComposeFile()
        }
    }

    override fun getDockerComposeFile(): File? {
        val dbName = DbUtil.databaseName(project)
        
        // For embedded databases like H2, no docker-compose file is needed
        if (DbUtil.isEmbeddedDatabase(dbName)) {
            return null
        }
        
        val resultComposeFilePath = DbUtil.getResolveDbFilePath(project)

        // Extract docker-compose file from plugin JAR for non-embedded databases only
        val src = DatabaseStartTask::class.java.protectionDomain.codeSource

        src?.let { codeSource ->
            File(resultComposeFilePath.parent.toFile().path, "$dbName-docker").mkdirs()

            val jar = codeSource.location
            val zip = ZipInputStream(jar.openStream())
            while (true) {
                val entry: ZipEntry? = zip.nextEntry

                if (entry != null) {
                    val name = entry.name

                    // Match docker-compose file for this database
                    val dockerComposeFileName = "database-compose/docker-compose_${dbName}.yaml"
                    if (name == dockerComposeFileName) {
                        FileUtil.copyFile(zip,
                            IntegrationServerUtil.getRelativePathInIntegrationServerDist(project, "docker-compose_${dbName}.yaml"))
                    }
                } else {
                    break
                }
            }
        }
        
        // Resolve placeholders like {{DB_PORT}}
        DbUtil.getResolvedDBDockerComposeFile(resultComposeFilePath, project)

        return project.file(resultComposeFilePath)
    }

    @TaskAction
    override fun run() {
        val dbName = DbUtil.databaseName(project)
        
        // Skip docker-compose for embedded databases like H2
        if (DbUtil.isEmbeddedDatabase(dbName)) {
            project.logger.lifecycle("Using embedded database $dbName - skipping docker-compose setup.")
            return
        }
        
        val composeFile = getDockerComposeFile()!!

        project.logger.lifecycle("Cleaning up previous database containers and networks.")
        execOperations.exec {
            executable = "docker-compose"
            args = listOf("-f", composeFile.path, "down", "--remove-orphans")
        }
        execOperations.exec {
            executable = "docker-compose"
            args = listOf("-f", composeFile.path, "up", "-d")
        }

        // `docker-compose up -d` returns as soon as the container has *started*, NOT when the
        // database inside it is actually accepting connections. With the old embedded databases
        // (Derby/H2) there was never such a gap. A containerized database (e.g. Postgres) still
        // needs time to run initdb after the container starts - and much more so on a cold agent
        // where the image has to be pulled first. If the XL Deploy server boots before the database
        // is ready, repository initialization fails and leaves the CLI descriptor registry in an
        // inconsistent state, surfacing later as "The type registry [...] for type [...] is not
        // registered". Blocking here until the DB reports healthy makes the startup deterministic
        // regardless of whether the docker image was warm or had to be pulled.
        waitForDatabaseReady(composeFile)

        if (dbName.startsWith("oracle")) {
            project.logger.lifecycle("Waiting for 1 minute to start oracle db")
            TimeUnit.SECONDS.sleep(60)
        }
    }

    /**
     * Polls the docker healthcheck status of the started database container(s) until they are
     * `healthy` or a timeout elapses. Containers whose compose service declares no healthcheck
     * report `none` and are treated as ready (preserving prior behaviour for images that do not
     * define one, e.g. oracle/mssql). The timeout can be tuned with `-PdatabaseReadinessTimeoutSeconds`.
     */
    private fun waitForDatabaseReady(composeFile: File) {
        val timeoutSeconds =
            PropertyUtil.resolveValue(project, "databaseReadinessTimeoutSeconds", "240").toString().toLong()
        val pollIntervalSeconds = 5L

        val containerIds = dockerStdout("docker-compose", listOf("-f", composeFile.path, "ps", "-q"))
            .lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (containerIds.isEmpty()) {
            project.logger.warn(
                "Could not determine database container id(s) from ${composeFile.name}; skipping readiness wait."
            )
            return
        }

        project.logger.lifecycle("Waiting up to ${timeoutSeconds}s for database container(s) to become healthy...")
        val deadline = System.currentTimeMillis() + timeoutSeconds * 1000
        while (true) {
            val statuses = containerIds.associateWith { id ->
                // Arguments are passed as a list (no shell), so the Go template survives verbatim on
                // every OS. Containers with no healthcheck report `none` and are treated as ready.
                dockerStdout(
                    "docker",
                    listOf("inspect", "-f", "{{if .State.Health}}{{.State.Health.Status}}{{else}}none{{end}}", id)
                ).trim()
            }
            val notReady = statuses.filterValues { it != "healthy" && it != "none" }
            if (notReady.isEmpty()) {
                project.logger.lifecycle("Database container(s) ready: $statuses")
                return
            }
            if (System.currentTimeMillis() >= deadline) {
                throw GradleException(
                    "Database container(s) did not become healthy within ${timeoutSeconds}s. " +
                        "Current status: $statuses. Inspect `docker logs <container>` for details."
                )
            }
            TimeUnit.SECONDS.sleep(pollIntervalSeconds)
        }
    }

    /**
     * Runs a docker/docker-compose command capturing ONLY stdout (stderr - e.g. the compose
     * `version is obsolete` warning - is left on the console so it cannot pollute the parsed
     * output). Arguments are passed as a list so no shell is involved and Go `--format` templates
     * are not mangled by shell quoting.
     */
    private fun dockerStdout(executableName: String, arguments: List<String>): String {
        val stdout = ByteArrayOutputStream()
        execOperations.exec {
            executable = executableName
            args = arguments
            standardOutput = stdout
            isIgnoreExitValue = true
        }
        return stdout.toString(StandardCharsets.UTF_8)
    }
}
