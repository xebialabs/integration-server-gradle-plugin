package ai.digital.integration.server.common.domain.providers.operator

import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

@Suppress("UnstableApiUsage")
open class OnPremiseProvider @Inject constructor(project: Project) : Provider(project) {

    @Input
    val driver = project.objects.property<String>().value("virtualbox")

    @Input
    val clusterNodeMemory = project.objects.property<Int>()

    @Input
    val clusterNodeCpus = project.objects.property<Int>()

    @Input
    val kubernetesVersion = project.objects.property<String>().value("1.20.0")

    @Input
    val skipExisting = project.objects.property<Boolean>().value(true)
}
