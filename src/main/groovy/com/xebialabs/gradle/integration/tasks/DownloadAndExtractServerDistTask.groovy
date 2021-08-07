package com.xebialabs.gradle.integration.tasks

import com.xebialabs.gradle.integration.domain.Server
import com.xebialabs.gradle.integration.util.ServerUtil
import org.gradle.api.tasks.Copy

import static com.xebialabs.gradle.integration.constant.PluginConstant.DIST_DESTINATION_NAME
import static com.xebialabs.gradle.integration.constant.PluginConstant.PLUGIN_GROUP
import static com.xebialabs.gradle.integration.util.ConfigurationsUtil.SERVER_DIST

class DownloadAndExtractServerDistTask extends Copy {
    static NAME = "downloadAndExtractServer"

    DownloadAndExtractServerDistTask() {
        this.configure {
            group = PLUGIN_GROUP

            def server = ServerUtil.getServer(project)

            if (isDownloadRequired(server)) {
                project.logger.lifecycle("Downloading and extracting the server.")
                project.buildscript.dependencies.add(
                        SERVER_DIST,
                        "com.xebialabs.deployit:xl-deploy-base:${server.version}:server@zip"
                )
                from { project.zipTree(project.buildscript.configurations.getByName(SERVER_DIST).singleFile) }
                into { project.buildDir.toPath().resolve(DIST_DESTINATION_NAME).toAbsolutePath().toString() }
            }
        }
    }

    private static def isDownloadRequired(Server server) {
        server.runtimeDirectory == null
    }
}
