package ai.digital.integration.server.deploy.tasks.permission

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.util.IntegrationServerUtil
import ai.digital.integration.server.deploy.internals.DeployConfigurationsUtil
import ai.digital.integration.server.deploy.internals.PermissionServiceUtil
import org.gradle.api.tasks.Copy

open class DownloadAndExtractPermissionDistTask : Copy() {

    init {
        this.group = PluginConstant.PLUGIN_GROUP
        val permissionServer = PermissionServiceUtil.getPermissionService(project)

        if (!PermissionServiceUtil.hasPermissionService(project)) {
            project.logger.lifecycle("Downloading and extracting the permission server ${permissionServer.version}.")

            project.buildscript.dependencies.add(
                    DeployConfigurationsUtil.PERMISSION_SERVICE_DIST,
                    "ai.digital.deploy:deploy-permission-service:${permissionServer.version}@zip"
            )
            this.from(project.zipTree(project.buildscript.configurations.getByName(DeployConfigurationsUtil.PERMISSION_SERVICE_DIST).singleFile))
            this.into(IntegrationServerUtil.getDist(project))
        }
    }

    companion object {
        const val NAME = "downloadAndExtractPermissionService"
    }
}
