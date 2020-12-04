package com.xebialabs.gradle.integration.tasks

import com.xebialabs.gradle.integration.util.ExtensionsUtil
import groovy.xml.XmlUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.xebialabs.gradle.integration.util.PluginUtil.PLUGIN_GROUP

class RemoveStdoutConfigTask extends DefaultTask {
    static NAME = "removeStdoutConfigTask"

    RemoveStdoutConfigTask() {
        this.configure { ->
            group = PLUGIN_GROUP
            mustRunAfter SetLogbackLevelsTask.NAME
        }
    }

    @TaskAction
    def RemoveStdoutConfig() {
        def removeStdoutConfig = ExtensionsUtil.getExtension(project).removeStdoutConfig
        if (removeStdoutConfig) {
            def logbackConfig = "${ExtensionsUtil.getServerWorkingDir(project)}/conf/logback.xml"
            def xml = new XmlParser().parse(project.file(logbackConfig))

            def stdoutAppender = xml.'**'.find { it["@name"] == 'STDOUT' }
            def stdoutRef = xml.'**'.find { it["@ref"] == 'STDOUT' }
            if (stdoutAppender) {
                stdoutAppender.parent().remove(stdoutAppender)
            }
            if (stdoutRef) {
                stdoutRef.parent().remove(stdoutRef)
            }
            XmlUtil.serialize(xml, new FileWriter(project.file(logbackConfig)))
        }
    }
}
