package ai.digital.integration.server.common.domain.providers.operator

import ai.digital.integration.server.common.util.ProcessUtil
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.property
import javax.inject.Inject


@Suppress("UnstableApiUsage")
open class AwsEksProvider @Inject constructor(val project: Project) : Provider(project) {

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
    val skipExisting = project.objects.property<Boolean>().value(true)

    fun getAwsAccessKey(): String {
        return if (project.hasProperty("accessKey") && project.property("accessKey") != null)
            project.property("accessKey").toString()
        else {
            val accessKey = ProcessUtil.executeCommand(project, "aws configure get aws_access_key_id --profile default", logOutput = false, throwErrorOnFailure = false)
            if (accessKey.isNullOrBlank()) {
                throw RuntimeException("Access key is required")
            }
            return accessKey
        }
    }

    fun getAwsSecretKey(): String {
        return if (project.hasProperty("secretKey") && project.property("secretKey") != null)
            project.property("secretKey").toString()
        else {
            val accessKey = ProcessUtil.executeCommand(project, "aws configure get aws_secret_access_key --profile default", logOutput = false)
            if (accessKey.isNullOrBlank()) {
                throw RuntimeException("Access Secret key is required")
            }
            return accessKey
        }
    }
}
