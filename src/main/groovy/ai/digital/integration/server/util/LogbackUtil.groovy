package ai.digital.integration.server.util

import groovy.xml.QName
import groovy.xml.XmlUtil
import org.gradle.api.Project

class LogbackUtil {

    private static def getHardCodedLevels(Project project) {
        DbUtil.getDatabase(project).logSql ? LogbackConfigs.toLogSql() : [:]
    }

    static def setLogLevels(Project project, String workingDir, Map<String, String> customLogLevels) {
        def logbackConfig = "${workingDir}/conf/logback.xml"
        def xml = new XmlParser().parse(project.file(logbackConfig))
        def configuration = xml.'**'.find { it.name() == 'configuration' }

        def logLevels = getHardCodedLevels(project) + customLogLevels
        logLevels.each { Map.Entry<String, String> logLevel ->
            configuration.appendNode(new QName("logger"), [name: logLevel.key, level: logLevel.value])
        }
        FileUtil.removeEmptyLines(XmlUtil.serialize(xml), project.file(logbackConfig))
    }
}
