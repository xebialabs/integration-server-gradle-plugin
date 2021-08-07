package com.xebialabs.gradle.integration.tasks.cli

import com.xebialabs.gradle.integration.domain.Server
import com.xebialabs.gradle.integration.tasks.DownloadAndExtractCliDistTask
import com.xebialabs.gradle.integration.tasks.StartIntegrationServerTask
import com.xebialabs.gradle.integration.tasks.database.ImportDbUnitDataTask
import com.xebialabs.gradle.integration.util.ConfigurationsUtil
import com.xebialabs.gradle.integration.util.ServerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.util.CollectionUtils

import static com.xebialabs.gradle.integration.constant.PluginConstant.PLUGIN_GROUP

class RunProvisionScriptTask extends DefaultTask {
    static NAME = "runProvisionScript"

    @Input
    String provisionScript = ""

    RunProvisionScriptTask() {
        def dependencies = [
                StartIntegrationServerTask.NAME,
                DownloadAndExtractCliDistTask.NAME
        ]

        this.configure {
            group = PLUGIN_GROUP
            dependsOn(dependencies)
            shouldRunAfter(StartIntegrationServerTask.NAME, ImportDbUnitDataTask.NAME)
        }
    }

    private void runProvisioning(Server server) {
        def filtered = project.configurations.getByName(ConfigurationsUtil.DEPLOY_CLI).filter { !it.name.endsWith("-sources.jar") }
        def classpath = CollectionUtils.join(File.pathSeparator, filtered.getFiles())

        logger.debug("Provision CLI classpath: \n${classpath}")

        def provisionScript = server.provisionScript
        def script = getProvisionScript() != null && !getProvisionScript().isEmpty() ? getProvisionScript() : server.provisionScript
        def port = server.httpPort
        def contextRoot = server.contextRoot

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
        project.logger.lifecycle("Running provision script on Deploy server.")
        runProvisioning(ServerUtil.getServer(project))
    }
}
