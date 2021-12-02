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
        ProcessUtil.execute(project, "aws", listOf("ecr", "create-repository", "--repository-name", "k8s/kube-bench", "--image-tag-mutability", "MUTABLE"), false)

        ProcessUtil.executeCommand(
                "aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin ${KubeScanningUtil.getAWSAccountId(project)}")
        ProcessUtil.execute(project, "docker", listOf("build", "-t", "k8s/kube-bench", KubeScanningUtil.getKubeBenchDir(project)), false)
        ProcessUtil.execute(project, "docker", listOf("tag", "k8s/kube-bench:latest", "${KubeScanningUtil.getAWSAccountId(project)}/k8s/kube-bench:latest"), false)
        ProcessUtil.execute(project, "docker", listOf("push", "${KubeScanningUtil.getAWSAccountId(project)}/k8s/kube-bench:latest"), false)

        updateKubeBenchImage()

        KubeCtlUtil.apply(File("${KubeScanningUtil.getKubeBenchDir(project)}/job-eks.yaml"))


    }

    private fun updateKubeBenchImage() {
        val file = File(KubeScanningUtil.getKubeBenchDir(project), "job-eks.yaml")
        val pairs = mutableMapOf<String, Any>(
                "spec.template.spec.containers[0].image" to "${KubeScanningUtil.getAWSAccountId(project)}/k8s/kube-bench:latest"
        )
       if(KubeScanningUtil.getKubeScanner(project).enableDebug){
            pairs.put("spec.template.spec.containers[0].command" , mutableListOf("kube-bench", "run", "--targets", "node", "--benchmark", "eks-1.0.1", "-v", "3","logtostrerr"))
        }
        YamlFileUtil.overlayFile(file, pairs)
    }

    @TaskAction
    fun launch() {
            kubeBenchPushAndApply()
            KubeScanningUtil.generateReport(project, "${reportFile}.log")
    }
}
