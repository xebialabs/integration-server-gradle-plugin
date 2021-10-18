package ai.digital.integration.server.deploy.tasks

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.deploy.util.DeployServerUtil
import ai.digital.integration.server.common.util.PropertiesUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class ApplicationConfigurationOverrideTask : DefaultTask() {

    init {
        this.group = PLUGIN_GROUP
        this.mustRunAfter("copyOverlays")
        this.mustRunAfter("centralConfiguration")
    }

    @TaskAction
    fun run() {
        project.logger.lifecycle("Configurations overriding overlaying files.")

        val deployitConf = project.file("${DeployServerUtil.getServerWorkingDir(project)}/conf/deployit.conf")

        val server = DeployServerUtil.getServer(project)
        val properties = PropertiesUtil.readPropertiesFile(deployitConf)
        properties["http.context.root"] = server.contextRoot
        properties["http.port"] = server.httpPort.toString()

        PropertiesUtil.writePropertiesFile(deployitConf, properties)
    }

    companion object {
        const val NAME = "applicationConfigurationOverride"
    }
}
