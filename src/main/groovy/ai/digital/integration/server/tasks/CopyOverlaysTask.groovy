package ai.digital.integration.server.tasks

import ai.digital.integration.server.domain.Server
import ai.digital.integration.server.util.DeployServerUtil
import ai.digital.integration.server.util.ExtensionUtil
import ai.digital.integration.server.util.OverlaysUtil
import ai.digital.integration.server.util.ServerUtil
import org.gradle.api.DefaultTask

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

class CopyOverlaysTask extends DefaultTask {
    public static String NAME = "copyOverlays"

    CopyOverlaysTask() {
        this.configure { ->
            group = PLUGIN_GROUP
            mustRunAfter DownloadAndExtractServerDistTask.NAME
            mustRunAfter CentralConfigurationTask.NAME
            mustRunAfter CopyServerBuildArtifactsTask.NAME
            finalizedBy CheckUILibVersionsTask.NAME

            project.afterEvaluate {
                Server server = DeployServerUtil.getServer(project)
                project.logger.lifecycle("Copying overlays on Deploy server ${server.name}")

                OverlaysUtil.addDatabaseDependency(project, server)
                OverlaysUtil.addMqDependency(project, server)

                server.overlays.each { Map.Entry<String, List<Object>> overlay ->
                    OverlaysUtil.defineOverlay(project, this, DeployServerUtil.getServerWorkingDir(project), ExtensionUtil.IS_EXTENSION_NAME, overlay, [])
                }
            }
        }
    }

}
