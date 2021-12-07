package ai.digital.integration.server.deploy.tasks.kube.scanning

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.util.*
import ai.digital.integration.server.common.util.KubeScanningUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.io.File

open class KubeAwsScannerTask : DefaultTask() {

    companion object {
        const val NAME = "kubeAwsScanner"
        const val reportFile = "aws-eks"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(CheckingOutKubeBenchTask.NAME)
        this.finalizedBy(KubeAwsScannerFinalizerTask.NAME)

    }

    private fun kubeBenchPushAndApply() {
        project.exec {
            executable = "cd"
            args = listOf(KubeScanningUtil.getKubeBenchDir(project))
        }

        createEcrRepoAndLogin()
        val ecrKubeBenchImage = "${KubeScanningUtil.getAWSAccountId(project)}/k8s/kube-bench:${KubeScanningUtil.getKubeScanner(project).kubeBenchTagVersion}"

        KubeScanningUtil.buildKubeBench(project)
        createECRImageTag(ecrKubeBenchImage)
        pushImageToECR(ecrKubeBenchImage)
        updateKubeBenchImage(ecrKubeBenchImage)

        KubeCtlUtil.apply(project, File("${KubeScanningUtil.getKubeBenchDir(project)}/job-eks.yaml"))
    }

    private fun createEcrRepoAndLogin() {
        ProcessUtil.executeCommand(project, "aws ecr --region ${KubeScanningUtil.getRegion(project)} create-repository --repository-name k8s/kube-bench --image-tag-mutability MUTABLE", logOutput = KubeScanningUtil.getKubeScanner(project).logOutput)

        ProcessUtil.executeCommand(project,
                "aws ecr --region ${KubeScanningUtil.getRegion(project)} get-login-password --region ${KubeScanningUtil.getRegion(project)} | docker login --username AWS --password-stdin ${KubeScanningUtil.getAWSAccountId(project)}",
                logOutput = KubeScanningUtil.getKubeScanner(project).logOutput)
    }

    private fun createECRImageTag(ecrKubeBenchImage: String) {
        ProcessUtil.executeCommand(project, "docker tag k8s/kube-bench:${KubeScanningUtil.getKubeScanner(project).kubeBenchTagVersion} $ecrKubeBenchImage", logOutput = KubeScanningUtil.getKubeScanner(project).logOutput)
    }

    private fun pushImageToECR(ecrKubeBenchImage: String) {
        ProcessUtil.executeCommand(project, "docker push $ecrKubeBenchImage", logOutput = KubeScanningUtil.getKubeScanner(project).logOutput)
    }

    private fun updateKubeBenchImage(ecrKubeBenchImage: String) {
        val file = File(KubeScanningUtil.getKubeBenchDir(project), "job-eks.yaml")
        val pairs = mutableMapOf<String, Any>(
                "spec.template.spec.containers[0].image" to ecrKubeBenchImage
        )
        if (KubeScanningUtil.getKubeScanner(project).enableDebug) {
            pairs.put("spec.template.spec.containers[0].command", mutableListOf("kube-bench", "run", "--targets", "node", "--benchmark", "eks-1.0.1", "-v", "3", "logtostrerr"))
        }
        YamlFileUtil.overlayFile(file, pairs)
    }

    @TaskAction
    fun launch() {
        kubeBenchPushAndApply()
        KubeScanningUtil.generateReport(project, "${reportFile}.log")
    }
}
