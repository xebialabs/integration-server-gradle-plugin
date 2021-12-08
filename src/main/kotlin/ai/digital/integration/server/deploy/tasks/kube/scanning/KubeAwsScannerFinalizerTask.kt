package ai.digital.integration.server.deploy.tasks.kube.scanning

import ai.digital.integration.server.common.util.KubeScanningUtil
import ai.digital.integration.server.common.util.ProcessUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

open class KubeAwsScannerFinalizerTask : DefaultTask() {

    companion object {
        const val NAME = "KubeAwsScannerFinalizer"
    }

    @TaskAction
    fun launch() {
        dockerLogout()
        deleteRepository()
        deleteEksJob()
        deleteDockerImage()
    }

    private fun deleteEksJob() {
        KubeScanningUtil.getKubectlHelper(project)
            .deleteFile(File("${KubeScanningUtil.getKubeBenchDir(project)}/job-eks.yaml"))
    }

    private fun dockerLogout() {
        ProcessUtil.executeCommand(project,
            "docker logout",
            logOutput = KubeScanningUtil.getKubeScanner(project).logOutput)
    }

    private fun deleteRepository() {
        ProcessUtil.executeCommand(project,
            "aws ecr --region ${KubeScanningUtil.getRegion(project)} delete-repository --repository-name k8s/kube-bench --force",
            logOutput = KubeScanningUtil.getKubeScanner(project).logOutput)
    }

    private fun deleteDockerImage() {
        ProcessUtil.executeCommand(project,
            "docker image rm ${KubeScanningUtil.getAWSAccountId(project)}/k8s/kube-bench:${
                KubeScanningUtil.getKubeScanner(project).kubeBenchTagVersion
            }", logOutput = KubeScanningUtil.getKubeScanner(project).logOutput)
        ProcessUtil.executeCommand(project,
            "docker image rm k8s/kube-bench:${KubeScanningUtil.getKubeScanner(project).kubeBenchTagVersion}",
            logOutput = KubeScanningUtil.getKubeScanner(project).logOutput)
    }
}
