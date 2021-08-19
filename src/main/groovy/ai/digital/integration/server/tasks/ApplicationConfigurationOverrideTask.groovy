package ai.digital.integration.server.tasks

import ai.digital.integration.server.domain.Server
import ai.digital.integration.server.util.PropertiesUtil
import ai.digital.integration.server.util.ServerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

class ApplicationConfigurationOverrideTask extends DefaultTask {
    static NAME = "applicationConfigurationOverride"

    ApplicationConfigurationOverrideTask() {
        this.configure { ->
            group = PLUGIN_GROUP
            mustRunAfter CopyOverlaysTask.NAME
        }
    }

    @TaskAction
    def run() {
        project.logger.lifecycle("Configurations overriding overlaying files.")

        def deployitConf = project.file("${ServerUtil.getServerWorkingDir(project)}/conf/deployit.conf")

        Server server = ServerUtil.getServer(project)
        def properties = PropertiesUtil.readPropertiesFile(deployitConf)
        properties.put("http.context.root", server.contextRoot)
        properties.put("http.port", server.httpPort.toString())

        PropertiesUtil.writePropertiesFile(deployitConf, properties)
    }
}
