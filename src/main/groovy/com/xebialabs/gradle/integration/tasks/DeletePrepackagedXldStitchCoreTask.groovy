package com.xebialabs.gradle.integration.tasks


import com.xebialabs.gradle.integration.util.LocationUtil
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.TaskAction

import static com.xebialabs.gradle.integration.constant.PluginConstant.PLUGIN_GROUP

class DeletePrepackagedXldStitchCoreTask extends Delete {
    static NAME = "deletePrepackagedStitchCore"

    DeletePrepackagedXldStitchCoreTask() {
        this.configure { ->
            group = PLUGIN_GROUP
            mustRunAfter DownloadAndExtractServerDistTask.NAME
        }
    }

    @TaskAction
    void deleteIfDuplicates() {
        project.logger.lifecycle("Deleting prepackaged Stitch Core on Deploy server")

        def baseDir = project.file("${LocationUtil.getServerWorkingDir(project)}/lib")
        def lib = baseDir.listFiles()

        lib.each { File file ->
            if (file.name.contains("xld-stitch-core-")) {
                project.delete file
                project.logger.lifecycle("Jar ${file} deleted.")
            }
        }
    }
}
