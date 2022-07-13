package ai.digital.integration.server.common.centralConfiguration

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.domain.CentralConfigurationServer
import ai.digital.integration.server.common.domain.Server
import ai.digital.integration.server.common.util.CentralConfigurationServerUtil
import ai.digital.integration.server.deploy.internals.DeployServerUtil
import ai.digital.integration.server.deploy.tasks.server.ServerYamlPatchTask
import org.apache.commons.io.FileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import java.nio.file.Paths

open class PrepareCentralConfigurationServerTask : DefaultTask() {

    init {
        this.group = PluginConstant.PLUGIN_GROUP

        if (DeployServerUtil.isDeployServerDefined(project)) {
            this.dependsOn(ServerYamlPatchTask.NAME)
        }

        this.onlyIf {
            CentralConfigurationServerUtil.hasCentralConfigurationServer(project)
        }
    }

    @TaskAction
    fun launch() {
        prepare(project)
    }

    fun prepare(project: Project) {
        val cc = CentralConfigurationServerUtil.getCentralConfigurationServer(project)
        val server = DeployServerUtil.getServer(project)
        project.logger.lifecycle("Preparing central configuration server ${cc.version} before launching it.")
        createConfFile(project, cc)
        copyCentralConfigurationDir(project, server, cc)
    }

    private fun copyCentralConfigurationDir(project: Project, server: Server, cc: CentralConfigurationServer) {
        project.logger.lifecycle("Copying centralConfiguration directory from ${server.name} to central configuration server")
        val sourceDir = Paths.get(DeployServerUtil.getServerWorkingDir(project), "centralConfiguration").toFile()
        val destinationDir = Paths.get(CentralConfigurationServerUtil.getServerPath(project, cc).toString(), "centralConfiguration").toFile()
        FileUtils.copyDirectory(sourceDir, destinationDir)
    }

    private fun createConfFile(project: Project, cc: CentralConfigurationServer) {
        project.logger.lifecycle("Creating deployit.conf file for central configuration server")

        val file = project.file("${CentralConfigurationServerUtil.getServerPath(project, cc)}/conf/deployit.conf")
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }
        file.createNewFile()

        file.appendText("http.port=${cc.httpPort}\n")
        file.appendText("xl.spring.cloud.encrypt.key=MQle?8_pwB^>f<&\n")
    }

    companion object {
        const val NAME = "prepareCentralConfigurationServer"
    }
}
