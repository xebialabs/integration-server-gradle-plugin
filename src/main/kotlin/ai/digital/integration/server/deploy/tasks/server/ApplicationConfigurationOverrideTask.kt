package ai.digital.integration.server.deploy.tasks.server

import ai.digital.integration.server.common.cluster.util.OperatorUtil
import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.common.util.PropertiesUtil
import ai.digital.integration.server.deploy.internals.DeployServerUtil
import ai.digital.integration.server.deploy.tasks.server.operator.OperatorCentralConfigurationTask
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class ApplicationConfigurationOverrideTask : DefaultTask() {

    init {
        this.group = PLUGIN_GROUP
        this.mustRunAfter(ServerCopyOverlaysTask.NAME)

        if (OperatorUtil(project).isClusterEnabled()) {
            this.mustRunAfter(OperatorCentralConfigurationTask.NAME)
        } else {
            this.mustRunAfter(CentralConfigurationTask.NAME)
        }
    }

    @TaskAction
    fun run() {
        DeployServerUtil.getServers(project)
            .forEach { server ->
                project.logger.lifecycle("Configurations overriding overlaying files for server ${server.name}.")

                val deployitConf =
                    project.file("${DeployServerUtil.getServerWorkingDir(project, server)}/conf/deployit.conf")


                val properties = PropertiesUtil.readPropertiesFile(deployitConf)
                properties["http.context.root"] = server.contextRoot
                properties["http.port"] = server.httpPort.toString()

                PropertiesUtil.writePropertiesFile(deployitConf, properties)
            }
    }

    companion object {
        const val NAME = "applicationConfigurationOverride"
    }
}
