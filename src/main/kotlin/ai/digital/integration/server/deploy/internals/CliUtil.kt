package ai.digital.integration.server.deploy.internals

import ai.digital.integration.server.common.domain.Test
import ai.digital.integration.server.common.util.IdUtil
import ai.digital.integration.server.common.util.IntegrationServerUtil
import ai.digital.integration.server.common.util.ProcessUtil
import ai.digital.integration.server.common.util.PropertyUtil
import ai.digital.integration.server.deploy.domain.Cli
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
import java.io.File
import java.nio.file.Paths
import kotlin.system.exitProcess

class CliUtil {
    companion object {

        fun getCli(project: Project): Cli {
            val cli = DeployExtensionUtil.getExtension(project).cli.get()
            cli.version = getCliVersion(project, cli)
            cli.debugPort = getDebugPort(project, cli)
            return cli
        }

        fun hasCli(project: Project): Boolean {
            return getCli(project).enabled
        }

        private fun getDebugPort(project: Project, cli: Cli): Int? {
            return if (PropertyUtil.resolveBooleanValue(project, "debug", true)) {
                PropertyUtil.resolveIntValue(project, "cliDebugPort", cli.debugPort)
            } else {
                null
            }
        }

        fun getWorkingDir(project: Project): String {
            val version = getCli(project).version
            val targetDir = IntegrationServerUtil.getDist(project)
            return Paths.get(targetDir, "xl-deploy-${version}-cli").toAbsolutePath().toString()
        }

        fun getCliLogFolder(project: Project): File {
            return File(getWorkingDir(project), "log")
        }

        fun getCliExtFolder(project: Project): File {
            return File(getWorkingDir(project), "ext")
        }

        private fun getCliLogFile(project: Project, label: String): File {
            val file = Paths.get("${getCliLogFolder(project)}/${label}-${IdUtil.shortId()}.log").toFile()
            project.file(file.parent).mkdirs()
            file.createNewFile()
            return file
        }

        private fun getCliBin(project: Project): File {
            return Paths.get(getWorkingDir(project), "bin").toFile()
        }

        private fun getCliVersion(project: Project, cli: Cli): String? {
            return when {
                project.hasProperty("deployCliVersion") -> {
                    project.property("deployCliVersion").toString()
                }
                !cli.version.isNullOrEmpty() -> {
                    cli.version
                }
                !DeployServerUtil.getServer(project).version.isNullOrEmpty() -> {
                    DeployServerUtil.getServer(project).version
                }
                else -> {
                    project.logger.error("CLI Version is not specified")
                    exitProcess(1)
                }
            }
        }

        fun executeScripts(project: Project, scriptSources: List<File>, label: String, secure: Boolean) {
            if (scriptSources.isNotEmpty()) {
                runScripts(project, scriptSources, label, secure, mapOf(), mapOf(), listOf())
            }
        }

        fun executeScripts(
            project: Project,
            scriptSources: List<File>,
            label: String,
            secure: Boolean,
            test: Test
        ) {
            runScripts(project,
                scriptSources,
                label,
                secure,
                test.environments,
                test.systemProperties,
                test.extraClassPath)
        }

        private fun runScripts(
            project: Project,
            scriptSources: List<File>,
            label: String,
            secure: Boolean,
            extraEnvironments: Map<String, String>,
            extraParams: Map<String, String?>,
            extraClassPath: List<File>
        ) {
            val cli = getCli(project)

            val extraParamsAsList = extraParams
                .filterValues { it != null }
                .map { arrayListOf(it.key, it.value) }
                .flatten()

            val params = (arrayListOf(
                "-context", EntryPointUrlUtil.getContextRoot(project),
                "-expose-proxies",
                "-password", "admin",
                "-port", EntryPointUrlUtil.getHttpPort(project),
                "-host", EntryPointUrlUtil.getHttpHost(),
                "-socketTimeout", cli.socketTimeout.toString(),
                "-source", scriptSources.joinToString(separator = ",") { source -> source.absolutePath },
                "-username", "admin"
            ) + extraParamsAsList).toMutableList()

            if (DeployServerUtil.isTls(project) || secure) {
                params += arrayOf("-secure")
            }

            val workDir = getCliBin(project)
            val scriptLogFile = getCliLogFile(project, label)

            val ext = if (Os.isFamily(Os.FAMILY_WINDOWS)) "cmd" else "sh"
            val commandLine = "$workDir ./cli.$ext ${params.joinToString(separator = " ")}"

            project.logger.lifecycle("Running this command now: $commandLine, logs can be found in $scriptLogFile")

            val environment = extraEnvironments + EnvironmentUtil.getCliEnv(project, cli, extraParams, extraClassPath)
            project.logger.info("Starting worker with environment: $environment")
            ProcessUtil.execAndCheck(
                mutableMapOf(
                    "command" to "cli",
                    "environment" to environment,
                    "params" to params,
                    "redirectTo" to scriptLogFile,
                    "wait" to true,
                    "workDir" to workDir
                ), scriptLogFile
            )
        }
    }
}
