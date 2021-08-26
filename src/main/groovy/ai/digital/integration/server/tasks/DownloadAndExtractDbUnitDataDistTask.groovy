package ai.digital.integration.server.tasks

import ai.digital.integration.server.util.ExtensionUtil
import ai.digital.integration.server.util.IntegrationServerUtil
import org.gradle.api.tasks.Copy

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP
import static ai.digital.integration.server.util.ConfigurationsUtil.SERVER_DATA_DIST

class DownloadAndExtractDbUnitDataDistTask extends Copy {
    static NAME = "downloadAndExtractDbUnitData"

    DownloadAndExtractDbUnitDataDistTask() {
        this.configure {
            group = PLUGIN_GROUP
            def version = ExtensionUtil.getExtension(project).xldIsDataVersion
            if (version != null) {
                project.buildscript.dependencies.add(
                        SERVER_DATA_DIST,
                        "com.xebialabs.deployit.plugins:xld-is-data:${version}:repository@zip"
                )
                from { project.zipTree(project.buildscript.configurations.getByName(SERVER_DATA_DIST).singleFile) }
                into { IntegrationServerUtil.getDist(project) }
            }
        }
    }
}
