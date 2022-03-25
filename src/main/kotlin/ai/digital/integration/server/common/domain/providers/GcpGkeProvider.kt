package ai.digital.integration.server.common.domain.providers

import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.property
import javax.inject.Inject


@Suppress("UnstableApiUsage")
open class GcpGkeProvider @Inject constructor(project: Project) : Provider(project) {

    @Input
    val accountName = project.objects.property<String>()

    fun getAccountCredFile(): String = project.property("accountCredFile").toString()

    @Input
    val projectName = project.objects.property<String>()

    @Input
    val regionZone = project.objects.property<String>()

    @Input
    val clusterNodeCount = project.objects.property<Int>().value(3)

    @Input
    val clusterNodeVmSize = project.objects.property<String>()

    @Input
    val kubernetesVersion = project.objects.property<String>()

    @Input
    val skipExisting = project.objects.property<Boolean>().value(true)
}
