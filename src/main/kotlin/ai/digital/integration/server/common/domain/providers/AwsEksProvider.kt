package ai.digital.integration.server.common.domain.providers

import ai.digital.integration.server.common.util.ProcessUtil
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.property
import javax.inject.Inject


@Suppress("UnstableApiUsage")
open class AwsEksProvider @Inject constructor(project: Project) : Provider(project) {

    @Input
    val region = project.objects.property<String>().value("us-east-1")

    @Input
    val stack = project.objects.property<String>().value("devops-operator-cluster-test-stack")

    @Input
    val clusterName = project.objects.property<String>().value("devops-operator-cluster-test-cluster")

    @Input
    val nodeGroupName = project.objects.property<String>().value("devops-operator-cluster-test-nodegroup")

    @Input
    val clusterNodeCount = project.objects.property<Int>().value(2)

    @Input
    val sshKeyName = project.objects.property<String>().value("devops-operator-cluster-test-ssh-key")

    @Input
    val fileSystemName = project.objects.property<String>().value("devops-operator-cluster-test-efs")

    @Input
    val kubernetesVersion = project.objects.property<String>().value("1.20")

    @Input
    val skipExisting = project.objects.property<Boolean>().value(true)

    @Input
    val stackTimeoutSeconds = project.objects.property<Int>().value(1500000)

    @Input
    val stackSleepTimeBeforeRetrySeconds = project.objects.property<Int>().value(300000)

    @Input
    val route53InsycAwaitTimeoutSeconds = project.objects.property<Int>().value(300000)

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
