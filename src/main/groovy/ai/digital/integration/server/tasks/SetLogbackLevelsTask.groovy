package ai.digital.integration.server.tasks

import ai.digital.integration.server.util.DbUtil
import ai.digital.integration.server.util.DeployServerUtil
import ai.digital.integration.server.util.LogbackUtil
import ai.digital.integration.server.util.ServerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

class SetLogbackLevelsTask extends DefaultTask {
    public static String NAME = "setLogbackLevels"

    SetLogbackLevelsTask() {
        this.configure { ->
            group = PLUGIN_GROUP
            mustRunAfter DownloadAndExtractServerDistTask.NAME
            onlyIf { !DeployServerUtil.isDockerBased(project) }
        }
    }

    @TaskAction
    def setLevels() {
        def server = DeployServerUtil.getServer(project)

        if (DbUtil.getDatabase(project).logSql || !server.logLevels.isEmpty()) {
            project.logger.lifecycle("Setting logback level on Deploy Server.")

            LogbackUtil.setLogLevels(project, DeployServerUtil.getServerWorkingDir(project), server.logLevels)
        }
    }
}
