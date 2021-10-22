package ai.digital.integration.server.common.util

import ai.digital.integration.server.deploy.config.LogbackConfigs
import org.gradle.api.Project
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File
import java.io.FileInputStream
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult


class LogbackUtil {

    companion object {

        private fun getHardCodedLevels(project: Project): MutableMap<String, String> {
            return if (DbUtil.getDatabase(project).logSql) LogbackConfigs.toLogSql else mutableMapOf()
        }

        fun setLogLevels(project: Project, workingDir: String, customLogLevels: Map<String, String>) {
            val logbackConfig = "${workingDir}/conf/logback.xml"

            val factory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
            val builder: DocumentBuilder = factory.newDocumentBuilder()
            val doc: Document =
                builder.parse(FileInputStream(logbackConfig)) // In My Case it's in the internal Storage

            val logLevels = getHardCodedLevels(project) + customLogLevels

            val configurationNode = doc.getElementsByTagName("configuration").item(0)

            logLevels.forEach { logLevel ->
                val logLevelNode: Element = doc.createElement("logger")
                logLevelNode.setAttribute("name", logLevel.key)
                logLevelNode.setAttribute("level", logLevel.value)
                configurationNode.appendChild(logLevelNode)
            }


            val transformerFactory: TransformerFactory = TransformerFactory.newInstance()
            val transf: Transformer = transformerFactory.newTransformer()

            transf.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
            transf.setOutputProperty(OutputKeys.INDENT, "yes")
            transf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")

            val source = DOMSource(doc)

            val logbackFile = File(logbackConfig)
            val logbackSreamResult = StreamResult(logbackFile)

            transf.transform(source, logbackSreamResult)
            FileUtil.removeEmptyLines(logbackFile.readText(Charsets.UTF_8), project.file(logbackConfig))
        }
    }
}
