package ai.digital.integration.server.tasks

import ai.digital.integration.server.domain.Server
import ai.digital.integration.server.util.FileUtil
import ai.digital.integration.server.util.ServerUtil
import org.apache.commons.io.FileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

class CopyBuildArtifactsTask extends DefaultTask {
    static NAME = "copyBuildArtifacts"

    CopyBuildArtifactsTask() {
        def dependencies = [
                'build',
                DownloadAndExtractServerDistTask.NAME
        ]

        this.configure { ->
            group = PLUGIN_GROUP
            dependsOn(dependencies)
        }
    }

    @TaskAction
    void launch() {
        Server server = ServerUtil.getServer(project)

        server.copyBuildArtifacts.each { Map.Entry<String, String> entry ->
            String where = entry.key
            String whatPattern = entry.value

            FileUtil.findFiles(project.buildDir.absolutePath, whatPattern, /\/[^\/]*integration-server\/[^\/]*/).each { File file ->
                FileUtils.copyFile(file, new File("${ServerUtil.getServerWorkingDir(project)}/${where}/${file.name}"))
            }
        }
    }
}
