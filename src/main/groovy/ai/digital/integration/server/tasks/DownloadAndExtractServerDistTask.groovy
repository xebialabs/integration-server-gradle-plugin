package ai.digital.integration.server.tasks


import ai.digital.integration.server.util.DeployServerUtil
import ai.digital.integration.server.util.IntegrationServerUtil
import ai.digital.integration.server.util.ServerUtil
import org.gradle.api.tasks.Copy

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP
import static ai.digital.integration.server.util.ConfigurationsUtil.SERVER_DIST

class DownloadAndExtractServerDistTask extends Copy {
    public static String NAME = "downloadAndExtractServer"


    DownloadAndExtractServerDistTask() {
        def dependencies = [
                PrepareDeployTask.NAME
        ]

        this.configure {
            def server = DeployServerUtil.getServer(project)

            group = PLUGIN_GROUP
            dependsOn(dependencies)

            if (ServerUtil.isDistDownloadRequired(project)) {
                project.logger.lifecycle("Downloading and extracting the server.")
                project.buildscript.dependencies.add(
                        SERVER_DIST,
                        "com.xebialabs.deployit:xl-deploy-base:${server.version}:server@zip"
                )
                from { project.zipTree(project.buildscript.configurations.getByName(SERVER_DIST).singleFile) }
                into { IntegrationServerUtil.getDist(project) }
            }
        }
    }
}
