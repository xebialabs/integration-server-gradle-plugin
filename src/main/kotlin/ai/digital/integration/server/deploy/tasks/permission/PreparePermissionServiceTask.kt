package ai.digital.integration.server.deploy.tasks.permission

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.deploy.internals.PermissionServiceInitializeUtil
import ai.digital.integration.server.deploy.tasks.maintenance.CleanupBeforeStartupTask
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class PreparePermissionServiceTask : DefaultTask() {

    init {
        group = PluginConstant.PLUGIN_GROUP

//        this.dependsOn(CleanupBeforeStartupTask.NAME)
    }

    @TaskAction
    fun launch() {
        PermissionServiceInitializeUtil.prepare(project)
    }

    companion object {
        const val NAME = "preparePermissionService"
    }
}
