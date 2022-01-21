package ai.digital.integration.server.deploy.tasks.server.operator

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.util.DockerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class StopDeployServerForOperatorUpgradeTask : DefaultTask() {
    companion object {
        const val NAME = "stopDeployServerForOperatorUpgrade"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
    }

    @TaskAction
    fun launch() {
        DockerUtil.execute(project, arrayListOf("stop", "dai-deploy"), logOutput = false, throwErrorOnFailure = false)
        DockerUtil.execute(project, arrayListOf("rm", "dai-deploy"), logOutput = false, throwErrorOnFailure = false)
    }
}
