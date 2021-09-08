package ai.digital.integration.server.util

import ai.digital.integration.server.domain.Cli
import ai.digital.integration.server.domain.Test
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project

import java.nio.file.Paths

class CliUtil {

    static Cli getCli(Project project) {
        List<Cli> clis = ExtensionUtil.getExtension(project).clis.toList()
        Cli cli = clis.isEmpty() ? new Cli("default") : clis.first()
        cli.setVersion(getCliVersion(project, cli))
        cli.setDebugPort(getDebugPort(project, cli))
        cli
    }

    static boolean hasCli(Project project) {
        !ExtensionUtil.getExtension(project).clis.isEmpty()
    }

    private static Integer getDebugPort(Project project, Cli cli) {
        if (PropertyUtil.resolveBooleanValue(project, "debug", true)) {
            PropertyUtil.resolveIntValue(project, "cliDebugPort", cli.debugPort)
        } else {
            null
        }
    }

    static def getWorkingDir(Project project) {
        String version = getCli(project).version
        def targetDir = IntegrationServerUtil.getDist(project).toString()
        Paths.get(targetDir, "xl-deploy-${version}-cli").toAbsolutePath().toString()
    }

    static File getCliLogFolder(Project project) {
        new File(getWorkingDir(project), "log")
    }

    static File getCliExtFolder(Project project) {
        new File(getWorkingDir(project), "ext")
    }

    static File getCliLogFile(Project project, String label) {
        def file = Paths.get("${getCliLogFolder(project)}/${label}-${IdUtil.shortId()}.log").toFile()
        project.file(file.getParent()).mkdirs()
        file.createNewFile()
        file
    }

    static def getCliBin(Project project) {
        Paths.get(getWorkingDir(project), "bin").toFile()
    }

    private static String getCliVersion(Project project, Cli cli) {
        if (project.hasProperty("deployCliVersion")) {
            project.getProperty("deployCliVersion")
        } else if (cli.version?.trim()) {
            cli.version
        } else if (ServerUtil.getServer(project).version) {
            ServerUtil.getServer(project).version
        } else {
            project.logger.error("CLI Version is not specified")
            System.exit(1)
            return null
        }
    }

    static def executeScripts(Project project, List<File> scriptSources, String label) {
        if (!scriptSources.isEmpty()) {
            runScripts(project, scriptSources, label, [:], [:], [])
        }
    }

    static def executeScripts(Project project,
                              List<File> scriptSources,
                              String label,
                              Test test) {
        runScripts(project, scriptSources, label, test.environments, test.systemProperties, test.extraClassPath)
    }

    private static def runScripts(Project project,
                                  List<File> scriptSources,
                                  String label,
                                  Map<String, String> extraEnvironments,
                                  Map<String, String> extraParams,
                                  List<File> extraClassPath) {
        Cli cli = getCli(project)

        def extraParamsAsList = extraParams.findAll {
            it -> it.value != null
        }.collect {
            it -> [it.key, it.value]
        }.flatten()

        def params = [
                "-context", ServerUtil.readDeployitConfProperty(project, "http.context.root"),
                "-expose-proxies",
                "-password", "admin",
                "-port", ServerUtil.readDeployitConfProperty(project, "http.port"),
                "-socketTimeout", cli.socketTimeout.toString(),
                "-source", scriptSources.collect { File source -> source.absolutePath }.join(","),
                "-username", "admin",
        ] + extraParamsAsList

        def workDir = getCliBin(project)
        def scriptLogFile = getCliLogFile(project, label)

        def ext = Os.isFamily(Os.FAMILY_WINDOWS) ? 'cmd' : 'sh'
        def commandLine = "${workDir} ./cli.$ext ${params.join(" ")}"

        project.logger.lifecycle("Running this command now: $commandLine, logs can be found in ${scriptLogFile}")

        ProcessUtil.execAndCheck([
                command    : "cli",
                environment: extraEnvironments + EnvironmentUtil.getCliEnv(cli, extraParams, extraClassPath),
                params     : params,
                redirectTo : scriptLogFile,
                wait       : true,
                workDir    : workDir
        ], scriptLogFile)
    }
}
