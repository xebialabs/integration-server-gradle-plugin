package ai.digital.integration.server.common.util

import ai.digital.integration.server.common.domain.MqParameters
import ai.digital.integration.server.deploy.util.DeployServerUtil
import org.gradle.api.Project
import java.nio.file.Path

class MqUtil {
    companion object {
        const val RABBITMQ = "rabbitmq"
        const val ACTIVEMQ = "activemq"

        fun getMqDirectory(project: Project): String {
            return FileUtil.toPathString(DeployServerUtil.getServerDistFolderPath(project), "mq")
        }

        fun mqName(project: Project): String {
            return PropertyUtil.resolveValue(project, "mq", RABBITMQ).toString()
        }

        fun mqPort(project: Project): Int? {
            return PropertyUtil.resolveIntValue(project, "mqPort", null)
        }

        fun getMqRelativePath(project: Project): String {
            return "mq/docker-compose_${mqName(project)}.yaml"
        }

        fun detectMqDependency(mq: String): MqParameters {
            return when (mq) {
                RABBITMQ -> rabbitmqPararms
                ACTIVEMQ -> activemqPararms
                else -> rabbitmqPararms
            }
        }

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

        val rabbitmqPararms = MqParameters(
            "com.rabbitmq.jms:rabbitmq-jms",
            "com.rabbitmq.jms.admin.RMQConnectionFactory",
            null,
            "guest",
            "guest"
        )

        val activemqPararms = MqParameters(
            "org.apache.activemq:activemq-client",
            "org.apache.activemq.ActiveMQConnectionFactory",
            null,
            "admin",
            "admin"
        )
    }
}
