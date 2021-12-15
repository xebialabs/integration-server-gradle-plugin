package ai.digital.integration.server.common.domain.providers.operator

import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.property
import javax.inject.Inject


@Suppress("UnstableApiUsage")
open class AwsEksProvider @Inject constructor(project: Project) : Provider(project) {

    @Input
    val region = project.objects.property<String>().value("us-east-1")

    @Input
    val stack = project.objects.property<String>().value("deploy-operator-test")

    @Input
    val clusterName = project.objects.property<String>().value("deploy-operator-cluster-test")

    @Input
    val nodeGroupName = project.objects.property<String>().value("deploy-operator-cluster-nodegroup")

    @Input
    val clusterNodeCount = project.objects.property<Int>().value(2)

    @Input
    val sshKeyName = project.objects.property<String>().value("deploy-operator-ssh-key")

    @Input
    val fileSystemName = project.objects.property<String>().value("deploy-operator-efs-test")

    @Input
    val kubernetesVersion = project.objects.property<String>().value("1.20")

    @Input
    val accessKey = project.objects.property<String>()

    @Input
    val accessSecret = project.objects.property<String>()

    @Input
    val skipExisting = project.objects.property<Boolean>().value(true)

}
