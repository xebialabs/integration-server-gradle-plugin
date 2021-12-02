package ai.digital.integration.server.deploy.tasks.kube.scanning

import ai.digital.integration.server.common.util.KubeCtlUtil
import ai.digital.integration.server.common.util.ProcessUtil
import ai.digital.integration.server.common.util.KubeScanningUtil

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

open class KubeAwsScannerFinalizerTask : DefaultTask() {

    companion object {
        const val NAME = "KubeAwsScannerFinalizer"
    }

    @TaskAction
    fun launch() {
        KubeCtlUtil.delete(project, File("${KubeScanningUtil.getKubeBenchDir(project)}/job-eks.yaml"))
        ProcessUtil.execute(project, "docker", listOf("logout"), false)
        ProcessUtil.executeCommand(
                "aws ecr delete-repository --repository-name k8s/kube-bench --force")
        ProcessUtil.executeCommand(
                "docker image rm ${KubeScanningUtil.getAWSAccountId(project)}/k8s/kube-bench")
        ProcessUtil.executeCommand(
                "docker image rm k8s/kube-bench")
    }

}