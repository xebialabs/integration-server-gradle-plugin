package ai.digital.integration.server.tasks

import ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.util.DbUtil
import ai.digital.integration.server.util.DeployServerUtil
import ai.digital.integration.server.util.LogbackUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class SetLogbackLevelsTask : DefaultTask() {

    companion object {
        @JvmStatic
        val NAME = "setLogbackLevels"
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
