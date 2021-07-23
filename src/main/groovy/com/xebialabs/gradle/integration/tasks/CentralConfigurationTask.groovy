package com.xebialabs.gradle.integration.tasks

import com.xebialabs.gradle.integration.util.DbUtil
import com.xebialabs.gradle.integration.util.ExtensionsUtil
import com.xebialabs.gradle.integration.util.YamlFileUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class CentralConfigurationTask extends DefaultTask {
    static NAME = "centralConfiguration"

    private void createCentralConfigurationFiles() {
        project.logger.lifecycle("Generating initial central configuration files")

        def extension = ExtensionsUtil.getExtension(project)
        YamlFileUtil.writeFileValue(
                new File("${ExtensionsUtil.getServerWorkingDir(project)}/centralConfiguration/deploy-repository.yaml"),
                DbUtil.dbConfig(project))

        YamlFileUtil.overlayFile(
                new File("${ExtensionsUtil.getServerWorkingDir(project)}/centralConfiguration/deploy-server.yaml"),
                ["deploy.server.port": extension.akkaRemotingPort]
        )
    }

    @TaskAction
    void launch() {
        createCentralConfigurationFiles()
    }
}
