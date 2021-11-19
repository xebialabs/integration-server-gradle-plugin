package ai.digital.integration.server.common.domain.providers.terraform

import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

@Suppress("UnstableApiUsage")
open class AwsEksProvider @Inject constructor(@Input val name: String, project: Project) : Provider {
    @Input
    val clusterVersion = project.objects.property<String>().value("1.17")

    @Input
    @Optional
    var vpcId = project.objects.property<String>()

    @Input
    var version = project.objects.property<String>().value("17.18.0")

    @Input
    @Optional
    var vpcVersion = project.objects.property<String>()

    @Input
    override var host = project.objects.property<String>()
}
