package ai.digital.integration.server.tasks

import ai.digital.integration.server.util.LocationUtil
import ai.digital.integration.server.util.ServerUtil
import groovy.xml.XmlUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

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
        def server = ServerUtil.getServer(project)
        project.logger.lifecycle("Removing STDOUT config on Deploy Server ${server.name}.")
        def removeStdoutConfig = server.removeStdoutConfig
        if (removeStdoutConfig) {
            def logbackConfig = "${LocationUtil.getServerWorkingDir(project)}/conf/logback.xml"
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
