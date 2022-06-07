package ai.digital.integration.server.deploy.tasks.kube.scanning

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.util.KubeScanningUtil
import ai.digital.integration.server.common.util.ProcessUtil
import ai.digital.integration.server.common.util.YamlFileUtil
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

    private fun kubeBenchBuildAndPushImage(ecrKubeBenchImage: String) {
        createEcrRepoAndLogin()
        KubeScanningUtil.buildKubeBench(project)
        createECRImageTag(ecrKubeBenchImage)
        pushImageToECR(ecrKubeBenchImage)
    }

    private fun createEcrRepoAndLogin() {
        ProcessUtil.executeCommand(project,
            "aws ecr --region ${KubeScanningUtil.getRegion(project)} create-repository --repository-name k8s/kube-bench --image-tag-mutability MUTABLE",
            logOutput = KubeScanningUtil.getKubeScanner(project).logOutput)

        ProcessUtil.executeCommand(project,
            "aws ecr --region ${KubeScanningUtil.getRegion(project)} get-login-password --region ${
                KubeScanningUtil.getRegion(project)
            } | docker login --username AWS --password-stdin ${KubeScanningUtil.getAWSAccountId(project)}",
            logOutput = KubeScanningUtil.getKubeScanner(project).logOutput)
    }

    private fun createECRImageTag(ecrKubeBenchImage: String) {
        ProcessUtil.executeCommand(project,
            "docker tag k8s/kube-bench:${KubeScanningUtil.getKubeScanner(project).kubeBenchTagVersion} $ecrKubeBenchImage",
            logOutput = KubeScanningUtil.getKubeScanner(project).logOutput)
    }

    private fun pushImageToECR(ecrKubeBenchImage: String) {
        ProcessUtil.executeCommand(project,
            "docker push $ecrKubeBenchImage",
            logOutput = KubeScanningUtil.getKubeScanner(project).logOutput)
    }

    @Suppress("UNCHECKED_CAST")
    private fun updateKubeBenchImage(ecrKubeBenchImage: String) {
        val file = File(KubeScanningUtil.getKubeBenchDir(project), "job-eks.yaml")
        val existingCommand =
            YamlFileUtil.readFileKey(file, "spec.template.spec.containers[0].command") as MutableList<String>
        val pairs = mutableMapOf(
            "spec.template.spec.containers[0].image" to ecrKubeBenchImage,
            "spec.template.spec.containers[0].command" to KubeScanningUtil.getCommand(project, existingCommand)
        )
        YamlFileUtil.overlayFile(file, pairs, minimizeQuotes = false)
    }

    @TaskAction
    fun launch() {
        val ecrKubeBenchImage =
            "${KubeScanningUtil.getAWSAccountId(project)}/k8s/kube-bench:${KubeScanningUtil.getKubeScanner(project).kubeBenchTagVersion}"
        kubeBenchBuildAndPushImage(ecrKubeBenchImage)
        updateKubeBenchImage(ecrKubeBenchImage)
        KubeScanningUtil.getKubectlHelper(project)
            .applyFile(File("${KubeScanningUtil.getKubeBenchDir(project)}/job-eks.yaml"))
        KubeScanningUtil.generateReport(project, "${reportFile}.log")
    }
}
