package ai.digital.integration.server.common.domain.providers.operator

import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.property
import javax.inject.Inject


@Suppress("UnstableApiUsage")
open class AzureAksProvider @Inject constructor(project: Project) : Provider(project) {

    @Input
    val location = project.objects.property<String>().value("germanywestcentral")

    @Input
    val clusterNodeCount = project.objects.property<Int>().value(2)

    @Input
    val clusterNodeVmSize = project.objects.property<String>()

    @Input
    val kubernetesVersion = project.objects.property<String>()

    @Input
    val skipExisting = project.objects.property<Boolean>().value(true)

    val azUsername = project.property("azUsername").toString()

    val azPassword = project.property("azPassword").toString()
}

