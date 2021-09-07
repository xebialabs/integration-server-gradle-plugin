package ai.digital.integration.server.tasks.worker

import ai.digital.integration.server.domain.Worker
import ai.digital.integration.server.util.DbUtil
import ai.digital.integration.server.util.FileUtil
import ai.digital.integration.server.util.LogbackConfigs
import ai.digital.integration.server.util.WorkerUtil
import groovy.xml.QName
import groovy.xml.XmlUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

class SetWorkersLogbackLevelsTask extends DefaultTask {
    static NAME = "setWorkerLogbackLevelsTask"

    SetWorkersLogbackLevelsTask() {
        this.configure { ->
            group = PLUGIN_GROUP
            mustRunAfter DownloadAndExtractWorkerDistTask.NAME, SyncServerPluginsWithWorkerTask.NAME
        }
    }

    private def getHardCodedLevels() {
        DbUtil.getDatabase(project).logSql ? LogbackConfigs.toLogSql() : [:]
    }

    @TaskAction
    def setWorkersLevels() {
        WorkerUtil.getWorkers(project).forEach { worker ->
            setWrokerLevels(worker)
        }
    }

    def setWrokerLevels(Worker worker) {
        if (DbUtil.getDatabase(project).logSql || !worker.logLevels.isEmpty()) {

            if (!worker.logLevels.isEmpty() && !WorkerUtil.isExternalRuntimeWorker(worker, project)) {
                logger.warn("Log levels settings on the worker ${worker.name} are ignored because worker's runtime directory is same to the master.")
            } else {
                project.logger.lifecycle("Setting logback level on worker ${worker.name}.")

                def logbackConfig = "${WorkerUtil.getWorkerWorkingDir(worker, project)}/conf/logback.xml"
                def xml = new XmlParser().parse(project.file(logbackConfig))
                def configuration = xml.'**'.find { it.name() == 'configuration' }

                def logLevels = getHardCodedLevels() + worker.logLevels
                logLevels.each { Map.Entry<String, String> logLevel ->
                    configuration.appendNode(new QName("logger"), [name: logLevel.key, level: logLevel.value])
                }
                FileUtil.removeEmptyLines(XmlUtil.serialize(xml), project.file(logbackConfig))
            }
        }
    }
}
