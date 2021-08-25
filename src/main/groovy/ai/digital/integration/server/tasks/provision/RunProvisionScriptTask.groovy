package ai.digital.integration.server.tasks.provision

import ai.digital.integration.server.domain.Server
import ai.digital.integration.server.tasks.DownloadAndExtractCliDistTask
import ai.digital.integration.server.tasks.StartIntegrationServerTask
import ai.digital.integration.server.tasks.database.ImportDbUnitDataTask
import ai.digital.integration.server.util.*
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.util.CollectionUtils

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

class RunProvisionScriptTask extends DefaultTask {
    static NAME = "runProvisionScript"

    // In case if you want to create a task directly
    @Input
    List<String> provisionScripts = List.of()

    RunProvisionScriptTask() {
        def dependencies = [
                StartIntegrationServerTask.NAME,
                DownloadAndExtractCliDistTask.NAME,
                ImportDbUnitDataTask.NAME
        ]

        this.configure {
            group = PLUGIN_GROUP
            dependsOn(dependencies)
        }
    }

    private void launchProvisionScripts(Server server) {
        List<String> scripts = getProvisionScripts().size() > 0 ? getProvisionScripts() : server.provisionScripts
        def port = server.httpPort
        def contextRoot = server.contextRoot
        scripts.each { String script ->
            if (server.runtimeDirectory != null) {
                def jars = project.configurations.getByName(ConfigurationsUtil.DEPLOY_CLI).filter { it.name.endsWith(".jar") }.getFiles()
                def zip = project.configurations.getByName(ConfigurationsUtil.DEPLOY_CLI).filter { it.name.endsWith(".zip") }.getSingleFile()

                def ant = new AntBuilder()
                def zipExtracted = "${project.buildDir}/cli/extracted"
                ant.unzip(src: zip, dest: zipExtracted, overwrite: true)

                def zipJars = project.fileTree(project.file(zipExtracted)).matching { include "**/*.jar" }.getFiles()

                def classpath = CollectionUtils.join(File.pathSeparator, (zipJars + jars).flatten())
                project.logger.lifecycle("Provision CLI classpath: \n${classpath}")
                executeScriptFromClassPath(contextRoot, port, script, classpath)
            } else {
                executeScript(server, script)
            }
        }
    }

    void executeScript(Server server, String script) {
        def params = [
                "-context", server.contextRoot,
                "-expose-proxies",
                "-password", "admin",
                "-port", server.httpPort.toString(),
                "-socketTimeout", server.provisionSocketTimeout.toString(),
                "-source", script,
                "-username", "admin",
        ]

        project.logger.lifecycle("Running provision script ${script} with parameters:${params}")

        ProcessUtil.exec([
                command    : "cli",
                environment: EnvironmentUtil.getCliEnv(server),
                params     : params,
                redirectTo : ServerUtil.getServerLogFile(project, "cli.log"),
                wait       : true,
                workDir    : CliUtil.getCliBin(project)
        ])
    }

    void executeScriptFromClassPath(String contextRoot, Integer port, String script, String classpath) {
        project.logger.lifecycle("Running provision script ${script} from classpath")
        project.javaexec {
            main = "com.xebialabs.deployit.cli.Cli"
            if (contextRoot.isEmpty()) {
                args '-q', '-expose-proxies', '-username', 'admin', '-password', 'admin', '-port', port, '-f', script
            } else {
                args '-context', contextRoot, '-q',
                        '-expose-proxies', '-username', 'admin', '-password', 'admin', '-port', port, '-f', script
            }
            environment "CLASSPATH", classpath
            jvmArgs '-Dlogback.config=src/main/resources/logback.xml'
        }
    }


    @TaskAction
    void launch() {
        project.logger.lifecycle("Running a CLI provision script on the Deploy server.")
        launchProvisionScripts(ServerUtil.getServer(project))
    }
}
