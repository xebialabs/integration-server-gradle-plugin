package com.xebialabs.gradle.integration.tasks

import com.xebialabs.gradle.integration.util.ExtensionsUtil
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.TaskAction

import static com.xebialabs.gradle.integration.util.PluginUtil.PLUGIN_GROUP

class DeletePrepackagedXldStitchCoreTask extends Delete {
    static NAME = "deletePrepackagedXldStitchCore"

    DeletePrepackagedXldStitchCoreTask() {
        this.configure { ->
            group = PLUGIN_GROUP
            mustRunAfter DownloadAndExtractServerDistTask.NAME
        }
    }

    @TaskAction
    void deleteIfDuplicates() {
        def baseDir = project.file("${ExtensionsUtil.getServerWorkingDir(project)}/lib")
        def lib = baseDir.listFiles()

        lib.each { file ->
            if (file.name.contains("xld-stitch-core-")) {
                project.delete file
                project.logger.lifecycle("Jar ${file} deleted.")
            }
        }
    }
}
