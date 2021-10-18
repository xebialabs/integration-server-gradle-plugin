package ai.digital.integration.server.deploy.tasks

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.common.util.DbUtil
import ai.digital.integration.server.deploy.util.DeployServerUtil
import ai.digital.integration.server.common.util.LogbackUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class SetLogbackLevelsTask : DefaultTask() {

    companion object {
        const val NAME = "setLogbackLevels"
    }

    init {
        this.group = PLUGIN_GROUP
        this.mustRunAfter(DownloadAndExtractServerDistTask.NAME)
        this.onlyIf { !DeployServerUtil.isDockerBased(project) }
    }

    @TaskAction
    fun setLevels() {
        val server = DeployServerUtil.getServer(project)

        if (DbUtil.getDatabase(project).logSql || server.logLevels.isNotEmpty()) {
            project.logger.lifecycle("Setting logback level on Deploy Server.")
            LogbackUtil.setLogLevels(project, DeployServerUtil.getServerWorkingDir(project), server.logLevels)
        }
    }
}
