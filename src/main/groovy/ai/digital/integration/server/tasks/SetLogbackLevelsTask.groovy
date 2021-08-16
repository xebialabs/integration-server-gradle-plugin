package ai.digital.integration.server.tasks

import ai.digital.integration.server.util.DbUtil
import ai.digital.integration.server.util.ServerUtil
import groovy.xml.QName
import groovy.xml.XmlUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

class SetLogbackLevelsTask extends DefaultTask {
    static NAME = "setLogbackLevels"

    SetLogbackLevelsTask() {
        this.configure { ->
            group = PLUGIN_GROUP
            mustRunAfter DownloadAndExtractServerDistTask.NAME
            onlyIf { !ServerUtil.isDockerBased(project) }
        }
    }

    private def getHardCodedLevels() {
        DbUtil.getDatabase(project).logSql ? [
                "org.hibernate.SQL" : "trace",
                "org.hibernate.type": "all"
        ] : [:]
    }

    @TaskAction
    def setLevels() {
        def server = ServerUtil.getServer(project)
        project.logger.lifecycle("Setting logback level on Deploy Server.")

        def logbackConfig = "${ServerUtil.getServerWorkingDir(project)}/conf/logback.xml"
        def xml = new XmlParser().parse(project.file(logbackConfig))
        def configuration = xml.'**'.find { it.name() == 'configuration' }

        def logLevels = getHardCodedLevels() + server.logLevels
        logLevels.each { Map.Entry<String, String> logLevel ->
            configuration.appendNode(new QName("logger"), [name: logLevel.key, level: logLevel.value])
        }
        XmlUtil.serialize(xml, new FileWriter(project.file(logbackConfig)))
    }
}
