package com.xebialabs.gradle.integration.tasks

import com.xebialabs.gradle.integration.util.ExtensionsUtil
import org.gradle.api.tasks.Copy

import static com.xebialabs.gradle.integration.util.ConfigurationsUtil.SERVER_DIST
import static com.xebialabs.gradle.integration.util.PluginUtil.DIST_DESTINATION_NAME
import static com.xebialabs.gradle.integration.util.PluginUtil.PLUGIN_GROUP

class DownloadAndExtractServerDistTask extends Copy {
    static NAME = "downloadAndExtractServer"

    DownloadAndExtractServerDistTask() {
        this.configure {
            group = PLUGIN_GROUP
            def downloadRequired = ExtensionsUtil.getExtension(project).serverRuntimeDirectory

            if (downloadRequired == null) {
                def serverVersion = ExtensionsUtil.getExtension(project).serverVersion
                project.buildscript.dependencies.add(
                        SERVER_DIST,
                        "com.xebialabs.deployit:xl-deploy-base:${serverVersion}:server@zip"
                )
                from { project.zipTree(project.buildscript.configurations.getByName(SERVER_DIST).singleFile) }
                into { project.buildDir.toPath().resolve(DIST_DESTINATION_NAME).toAbsolutePath().toString() }
            }

        }
    }
}
