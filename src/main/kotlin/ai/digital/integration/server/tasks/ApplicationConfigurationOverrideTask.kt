package ai.digital.integration.server.tasks

import ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.util.DeployServerUtil
import ai.digital.integration.server.util.PropertiesUtil
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
        properties.put("http.context.root", server.contextRoot)
        properties.put("http.port", server.httpPort.toString())

        PropertiesUtil.writePropertiesFile(deployitConf, properties)
    }

    companion object {
        @JvmStatic
        val NAME = "applicationConfigurationOverride"
    }
}
