package com.xebialabs.gradle.integration.tasks

import com.xebialabs.gradle.integration.util.ExtensionsUtil
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.TaskAction

import static com.xebialabs.gradle.integration.util.PluginUtil.PLUGIN_GROUP

class DeleteDuplicatedJarsTask extends Delete {
    static NAME = "deleteDuplicatedJars"


    DeleteDuplicatedJarsTask() {
        this.configure { ->
            group = PLUGIN_GROUP
            dependsOn CopyOverlaysTask.NAME
            project.afterEvaluate {
                this.dependsOn CopyOverlaysTask.NAME
            }
        }
    }

    private static def getStitchVarsion(project, libsSize) {
        def ext = ExtensionsUtil.getExtension(project)
        if (!ext.hasStitchCoreVersion.toBoolean() && libsSize > 1) {
            project.version
        } else {
            ext.stitchCoreVersion
        }
    }

    @TaskAction
    void deleteIfDuplicates() {
        def overlayLibSize = ExtensionsUtil.getExtension(project).overlays.get("lib").size()
        def baseDir = project.file("${ExtensionsUtil.getServerWorkingDir(project)}/lib")
        def lib = baseDir.listFiles()
        def version = getStitchVarsion(project, overlayLibSize)

        lib.each { file ->
            def shouldDeleteStitchCoreVersion = file.name.contains("xld-stitch-core-") &&
                    !file.name.contains("xld-stitch-core-${version}")
            if (overlayLibSize > 1 && shouldDeleteStitchCoreVersion) {
                project.logger.lifecycle("Jar ${file} deleted.")
                project.delete file
            }
        }
    }
}
