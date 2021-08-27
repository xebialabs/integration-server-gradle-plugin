package ai.digital.integration.server.util

import ai.digital.integration.server.domain.Cli
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

    static def getCliLogFile(Project project, String script) {
        def file = Paths.get("${getWorkingDir(project)}/log/${getCliLogName(project, script)}").toFile()
        project.file(file.getParent()).mkdirs()
        file.createNewFile()
        file
    }

    private static def getCliLogName(Project project, String script) {
        String version = getCli(project).version
        "xl-deploy-${version}-cli-${script}.log"
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
}
