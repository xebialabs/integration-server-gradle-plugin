package ai.digital.integration.server.tasks


import ai.digital.integration.server.util.ServerUtil
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.TaskAction

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

//TODO: Refactor it, it has to be done in a generic way of excluding any lib/plugin from the dist
class DeletePrepackagedXldStitchCoreTask extends Delete {
    static NAME = "deletePrepackagedStitchCore"

    DeletePrepackagedXldStitchCoreTask() {
        this.configure { ->
            group = PLUGIN_GROUP
            mustRunAfter DownloadAndExtractServerDistTask.NAME
            onlyIf {
                !ServerUtil.isDockerBased(project)
            }
        }
    }

    @TaskAction
    void deleteIfDuplicates() {
        project.logger.lifecycle("Deleting prepackaged Stitch Core on Deploy server")

        def baseDir = project.file("${ServerUtil.getServerWorkingDir(project)}/lib")
        def lib = baseDir.listFiles()

        lib.each { File file ->
            if (file.name.contains("xld-stitch-core-")) {
                project.delete file
                project.logger.lifecycle("Jar ${file} deleted.")
            }
        }
    }
}
