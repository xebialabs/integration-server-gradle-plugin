package ai.digital.integration.server.common.domain.providers.terraform

import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

@Suppress("UnstableApiUsage")
open class AwsEksProvider @Inject constructor(@Input val name: String, project: Project) : Provider {

    @Input
    val clusterName = project.objects.property<String>().value("terraform-aws-eks")

    @Input
    val clusterVersion = project.objects.property<String>().value("1.17")

    @Input
    @Optional
    var source = project.objects.property<String>().value("terraform-aws-modules/eks/aws")

    @Input
    var version = project.objects.property<String>().value("17.18.0")

    @Input
    @Optional
    var vpcName = project.objects.property<String>().value("2.78.0")

    @Input
    @Optional
    var vpcSource = project.objects.property<String>().value("terraform-aws-modules/vpc/aws")

    @Input
    @Optional
    var vpcVersion = project.objects.property<String>()

    @Input
    override var host = project.objects.property<String>().value("localhost")
}
