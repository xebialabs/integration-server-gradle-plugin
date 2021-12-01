package ai.digital.integration.server.deploy.tasks.cluster.k8sinstaller.scanning

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.util.*
import ai.digital.integration.server.deploy.internals.KubeBenchUtil
import net.sf.json.JSONObject
import net.sf.json.util.JSONTokener
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.io.File

open class KubeBenchInstallerAwsEksTask : DefaultTask() {

    companion object {
        const val NAME = "kubeBenchInstallerAwsEksTask"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(CheckingOutKubeBenchTask.NAME)
    }

    private lateinit var account: String

    private fun kubeBenchPushAndApply() {
        project.exec {
            executable = "cd"
            args = listOf(KubeBenchUtil.getKubeBenchDir(project))
        }
        ProcessUtil.execute(project, "aws", listOf("ecr", "create-repository", "--repository-name", "k8s/kube-bench", "--image-tag-mutability", "MUTABLE"), true)

        ProcessUtil.executeCommand(project,
                "aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin ${account}.dkr.ecr.us-east-1.amazonaws.com")
        ProcessUtil.execute(project, "docker", listOf("build", "-t", "k8s/kube-bench", KubeBenchUtil.getKubeBenchDir(project)), true)
        ProcessUtil.execute(project, "docker", listOf("tag", "k8s/kube-bench:latest", "${account}.dkr.ecr.us-east-1.amazonaws.com/k8s/kube-bench:latest"), true)
        ProcessUtil.execute(project, "docker", listOf("push", "${account}.dkr.ecr.us-east-1.amazonaws.com/k8s/kube-bench:latest"), true)

        updateKubeBenchImage()
        KubeCtlUtil.apply(project, File("${KubeBenchUtil.getKubeBenchDir(project)}/job-eks.yaml"))
        ProcessUtil.execute(project, "docker", listOf("logout"), true)
    }

    private fun updateKubeBenchImage() {
        val file = File(KubeBenchUtil.getKubeBenchDir(project), "job-eks.yaml")
        val pairs = mutableMapOf<String, Any>(
                "spec.template.spec.containers[0].image" to "${account}.dkr.ecr.us-east-1.amazonaws.com/k8s/kube-bench:latest"
        )
        YamlFileUtil.overlayFile(file, pairs)
    }

    private fun cleanUp() {
        ProcessUtil.executeCommand(project,
                "aws ecr delete-repository --repository-name k8s/kube-bench --force")
        ProcessUtil.executeCommand(project,
                "docker image rm 932770550094.dkr.ecr.us-east-1.amazonaws.com/k8s/kube-bench")
        ProcessUtil.executeCommand(project,
                "docker image rm k8s/kube-bench")
    }

    @TaskAction
    fun launch() {
        val identityDetail: String = ProcessUtil.execute(project, "aws", listOf("sts", "get-caller-identity"), true)
        val identity = JSONTokener(identityDetail).nextValue() as JSONObject
        account = identity.get("Account").toString()

        kubeBenchPushAndApply()
        KubeBenchUtil.generateReport(project, "Aws_Eks_Report.log")
        KubeCtlUtil.delete(project, File("${KubeBenchUtil.getKubeBenchDir(project)}/job-eks.yaml"))
        cleanUp()
    }
}
