package com.xebialabs.gradle.integration.tasks

import com.xebialabs.gradle.integration.util.ExtensionsUtil
import groovy.xml.QName
import groovy.xml.XmlUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.xebialabs.gradle.integration.util.PluginUtil.PLUGIN_GROUP

class SetLogbackLevelsTask extends DefaultTask {
    static NAME = "setLogbackLevels"

    SetLogbackLevelsTask() {
        this.configure { ->
            group = PLUGIN_GROUP
            mustRunAfter DownloadAndExtractServerDistTask.NAME
        }
    }

    @TaskAction
    def setLevels() {
        def logLevels = ExtensionsUtil.getExtension(project).logLevels
        if (logLevels && logLevels.size() > 0) {
            def logbackConfig = "${ExtensionsUtil.getServerWorkingDir(project)}/conf/logback.xml"
            def xml = new XmlParser().parse(project.file(logbackConfig))

            def configuration = xml.'**'.find { it.name() == 'configuration' }
            logLevels.each { logLevel ->
                configuration.appendNode(new QName("logger"), [name: logLevel.key, level: logLevel.value])
            }
            XmlUtil.serialize(xml, new FileWriter(project.file(logbackConfig)))
        }
    }
}
