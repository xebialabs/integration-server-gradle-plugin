package ai.digital.integration.server.common.domain.providers

import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.property
import java.time.Duration
import javax.inject.Inject

@Suppress("UnstableApiUsage")
abstract class Provider @Inject constructor(val project: Project) {

    @Input
    val host = project.objects.property<String>()

    @Input
    val keystorePassphrase = project.objects.property<String>().value("test123")

    @Input
    val name = project.objects.property<String>()

    @Input
    val operatorImage = project.objects.property<String>()

    @Input
    val operatorBranch = project.objects.property<String>()

    @Input
    val helmBranch = project.objects.property<String>().value("master")

    @Input
    val helmXldReleaseName = project.objects.property<String>().value("dai-xld")

    @Input
    val helmXlrReleaseName = project.objects.property<String>().value("dai-xlr")

    @Input
    val operatorPackageVersion = project.objects.property<String>()

    @Input
    val repositoryKeystore = project.objects.property<String>().value("zs7OzgAAAAIAAAABAAAAAwAWZGVwbG95aXQtcGFzc3N3b3JkLWtleQAAAX0PJb+CrO0ABXNyADNjb20uc3VuLmNyeXB0by5wcm92aWRlci5TZWFsZWRPYmplY3RGb3JLZXlQcm90ZWN0b3LNV8pZ5zC7UwIAAHhyABlqYXZheC5jcnlwdG8uU2VhbGVkT2JqZWN0PjY9psO3VHACAARbAA1lbmNvZGVkUGFyYW1zdAACW0JbABBlbmNyeXB0ZWRDb250ZW50cQB+AAJMAAlwYXJhbXNBbGd0ABJMamF2YS9sYW5nL1N0cmluZztMAAdzZWFsQWxncQB+AAN4cHVyAAJbQqzzF/gGCFTgAgAAeHAAAAARMA8ECJ5qQHT9gnDqAgMDDUB1cQB+AAUAAACQmtLr3iZ5MZZg1CaZ5+D3HW2x1ao5yGvSgxC085MShmXOGxFGRr0lLMbpTabZiXYxGYYWUhc6lcjgPFTg7JUvpWC8pD85tSAiMAHe9VaBQ7GWoFUhQz0WcZQZZkKztJkp7EzQ4zw+RYBI1yoHdWXSauEJaGb1lKy+uZQiWhSzF/5pI0pEiTC3uBvS4Deq0GMDdAAWUEJFV2l0aE1ENUFuZFRyaXBsZURFU3QAFlBCRVdpdGhNRDVBbmRUcmlwbGVERVNvcQWLkwe757iGUL7iAKvi4D4ghg==")

    @Input
    val storageClass = project.objects.property<String>()

    @Input
    val rabbitmqReplicaCount = project.objects.property<Int>().value(1)

    @Input
    val destroyClusterOnShutdown = project.objects.property<Boolean>().value(false)

    @Input
    val cleanUpWaitTimeout = project.objects.property<Duration>().value(Duration.ofSeconds(90))

    @Input
    val maxDbConnections = project.objects.property<Int>()

}
