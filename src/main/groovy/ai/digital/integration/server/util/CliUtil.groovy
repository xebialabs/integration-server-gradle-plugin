package ai.digital.integration.server.util

import ai.digital.integration.server.domain.Cli
import ai.digital.integration.server.domain.Server
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

    private static Integer getDebugPort(Project project, Cli cli) {
        project.hasProperty("cliDebugPort") ? Integer.valueOf(project.property("cliDebugPort").toString()) : cli.debugPort
    }

    static def getWorkingDir(Project project) {
        String version = getCli(project).version
        def targetDir = IntegrationServerUtil.getDist(project).toString()
        Paths.get(targetDir, "xl-deploy-${version}-cli").toAbsolutePath().toString()
    }

    static File getCliLogFolder(Project project) {
        new File(getWorkingDir(project), "log")
    }

    static File getCliLogFile(Project project, File scriptSource) {
        def file = Paths.get("${getCliLogFolder(project)}/${scriptSource.name}-${IdUtil.shortId()}.log").toFile()
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
        } else if (project.hasProperty("xlDeployVersion")) {
            project.getProperty("xlDeployVersion")
        } else {
            project.logger.error("CLI Version is not specified")
            System.exit(1)
            return null
        }
    }

    static def executeScript(Project project, File scriptSource) {
        Server server = ServerUtil.getServer(project)
        Cli cli = getCli(project)

        def params = [
                "-context", server.contextRoot,
                "-expose-proxies",
                "-password", "admin",
                "-port", server.httpPort.toString(),
                "-socketTimeout", cli.socketTimeout.toString(),
                "-source", scriptSource.absolutePath,
                "-username", "admin",
        ]

        def workDir = getCliBin(project)
        def scriptLogFile = getCliLogFile(project, scriptSource)

        def ext = Os.isFamily(Os.FAMILY_WINDOWS) ? 'cmd' : 'sh'
        def commandLine = "${workDir} ./cli.$ext ${params.join(" ")}"

        project.logger.lifecycle("Running this command now: $commandLine, logs can be found in ${scriptLogFile}")

        ProcessUtil.execAndCheck([
                command    : "cli",
                environment: EnvironmentUtil.getCliEnv(cli),
                params     : params,
                redirectTo : scriptLogFile,
                wait       : true,
                workDir    : workDir
        ], scriptLogFile)
    }
}
