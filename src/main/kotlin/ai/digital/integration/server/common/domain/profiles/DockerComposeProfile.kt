package ai.digital.integration.server.common.domain.profiles

import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.property

@Suppress("UnstableApiUsage")
open class DockerComposeProfile(objects: ObjectFactory) {

    var rabbitMqImage: String = objects.property<String>().value("rabbitmq:3.9.8-management-alpine").get()
    var postgresImage: String = objects.property<String>().value("postgres:10.5").get()
    var postgresCommand: String = objects.property<String>().value("postgres -c 'max_connections=300'").get()

}
