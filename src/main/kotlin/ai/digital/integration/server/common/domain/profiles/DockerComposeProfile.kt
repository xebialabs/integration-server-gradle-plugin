package ai.digital.integration.server.common.domain.profiles

import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

@Suppress("UnstableApiUsage")
open class DockerComposeProfile @Inject constructor(project: Project) : Profile {

    val name: String = "dockerCompose"

    @Input
    val rabbitMqImage = project.objects.property<String>().value("rabbitmq:3.9.8-management-alpine")

    @Input
    val postgresImage = project.objects.property<String>().value("postgres:10.5")

    @Input
    val postgresCommand = project.objects.property<String>().value("postgres -c 'max_connections=300'")
}
