package com.xebialabs.gradle.integration.tasks

import com.xebialabs.gradle.integration.IntegrationServerExtension
import static com.xebialabs.gradle.integration.util.ConfigurationsUtil.SERVER_DIST_CONFIG
import org.gradle.api.tasks.Copy

class DownloadAndExtractServerDist extends Copy {
    DownloadAndExtractServerDist() {
        this.configure {
            group = "Integration server"
            def serverVersion = project.extensions.getByType(IntegrationServerExtension).serverVersion
            project.buildscript.dependencies.add(
                    SERVER_DIST_CONFIG,
                    "com.xebialabs.deployit:xl-deploy-base:${serverVersion}:server@zip"
            )
            from { project.zipTree(project.buildscript.configurations.getByName(SERVER_DIST_CONFIG).singleFile) }
            into { project.buildDir.toPath().resolve("integration-server").toAbsolutePath().toString() }
        }
    }
}
