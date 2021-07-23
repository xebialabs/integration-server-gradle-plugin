package com.xebialabs.gradle.integration.tasks.cli

import com.xebialabs.gradle.integration.tasks.DownloadAndExtractCliDistTask
import com.xebialabs.gradle.integration.tasks.StartIntegrationServerTask

import com.xebialabs.gradle.integration.tasks.database.ImportDbUnitDataTask
import com.xebialabs.gradle.integration.util.ConfigurationsUtil
import com.xebialabs.gradle.integration.util.ExtensionsUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.util.CollectionUtils


import static com.xebialabs.gradle.integration.util.PluginUtil.PLUGIN_GROUP

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

    private void runProvisioning() {

        def filtered = project.configurations.getByName(ConfigurationsUtil.INTEGRATION_TEST_CLI).filter { !it.name.endsWith("-sources.jar") }
        def classpath = CollectionUtils.join(File.pathSeparator, filtered.getFiles())

        logger.debug("Provision CLI classpath: \n${classpath}")
        def extension = ExtensionsUtil.getExtension(project)
        def script = getProvisionScript() != null && !getProvisionScript().isEmpty()? getProvisionScript() : extension.getProvisionScript()
        project.javaexec {
            main = "com.xebialabs.deployit.cli.Cli"
            if (extension.getServerContextRoot().isEmpty()) {
                args '-q', '-expose-proxies', '-username', 'admin', '-password', 'admin', '-port', "${extension.getServerHttpPort()}", '-f', extension.getProvisionScript()
            } else {
                args '-context', extension.getServerContextRoot(), '-q', '-expose-proxies', '-username', 'admin', '-password', 'admin', '-port',
                "${extension.getServerHttpPort()}" , '-f' , script
            }
            environment "CLASSPATH", classpath
            jvmArgs '-Dlogback.config=src/main/resources/logback.xml'
        }

    }


    @TaskAction
    void launch() {
        runProvisioning()
    }
}
