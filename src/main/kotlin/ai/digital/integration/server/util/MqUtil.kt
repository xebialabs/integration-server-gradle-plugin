package ai.digital.integration.server.util

import ai.digital.integration.server.domain.MqParameters
import org.gradle.api.Project
import java.nio.file.Path

class MqUtil {
    companion object {
        @JvmStatic
        val RABBITMQ = "rabbitmq"

        @JvmStatic
        val ACTIVEMQ = "activemq"

        @JvmStatic
        fun getMqDirectory(project: Project): String {
            return FileUtil.toPathString(DeployServerUtil.getServerDistFolderPath(project), "mq")
        }

        @JvmStatic
        fun mqName(project: Project): String {
            return PropertyUtil.resolveValue(project, "mq", RABBITMQ).toString()
        }

        @JvmStatic
        fun mqPort(project: Project): Int? {
            return PropertyUtil.resolveIntValue(project, "mqPort", null)
        }

        @JvmStatic
        fun getMqRelativePath(project: Project): String {
            return "mq/docker-compose_${mqName(project)}.yaml"
        }

        @JvmStatic
        fun detectMqDependency(mq: String): MqParameters {
            return when (mq) {
                RABBITMQ -> rabbitmqPararms
                ACTIVEMQ -> activemqPararms
                else -> rabbitmqPararms
            }
        }

        @JvmStatic
        fun getResolvedDockerFile(project: Project): Path {
            val resultComposeFilePath = DockerComposeUtil.getResolvedDockerPath(project, getMqRelativePath(project))

            val mqTemplate = resultComposeFilePath.toFile()
            val port = if (mqName(project) == RABBITMQ) 5672 else 61616

            val resolvedMqPort = PropertyUtil.resolveIntValue(project, "mqPort", port)

            val configuredTemplate = mqTemplate.readText()
                .replace("RABBITMQ_PORT2", "${resolvedMqPort}:5672")
                .replace("ACTIVEMQ_PORT2", "${resolvedMqPort}:61616")

            mqTemplate.writeText(configuredTemplate)

            return resultComposeFilePath
        }

        @JvmStatic
        val rabbitmqPararms = MqParameters(
            "com.rabbitmq.jms:rabbitmq-jms",
            "com.rabbitmq.jms.admin.RMQConnectionFactory",
            null,
            "guest",
            "guest"
        )

        @JvmStatic
        val activemqPararms = MqParameters(
            "org.apache.activemq:activemq-client",
            "org.apache.activemq.ActiveMQConnectionFactory",
            null,
            "admin",
            "admin"
        )
    }
}
