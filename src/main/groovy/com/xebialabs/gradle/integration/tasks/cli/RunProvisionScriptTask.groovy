package com.xebialabs.gradle.integration.tasks.cli

import com.xebialabs.gradle.integration.tasks.DownloadAndExtractCliDistTask
import com.xebialabs.gradle.integration.tasks.StartIntegrationServerTask

import com.xebialabs.gradle.integration.tasks.database.ImportDbUnitDataTask
import com.xebialabs.gradle.integration.util.ExtensionsUtil
import com.xebialabs.gradle.integration.util.ProcessUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.util.CollectionUtils

import java.nio.file.Paths

import static com.xebialabs.gradle.integration.util.PluginUtil.PLUGIN_GROUP

class RunProvisionScriptTask extends DefaultTask {
    static NAME = "runProvisionScript"
    String configurationName = 'integrationTestCli'
    String provisionScript

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

        def filtered = project.configurations.getByName(configurationName).filter { !it.name.endsWith("-sources.jar") }
        def classpath = CollectionUtils.join(File.pathSeparator, filtered.getFiles())

        logger.debug("Provision CLI classpath: \n${classpath}")
        def extension = ExtensionsUtil.getExtension(project)
        def script = getProvisionScript() != null ? getProvisionScript() : extension.getProvisionScript()
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
