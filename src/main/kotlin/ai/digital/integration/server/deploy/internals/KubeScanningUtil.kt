package ai.digital.integration.server.deploy.internals

import ai.digital.integration.server.common.domain.Cluster
import ai.digital.integration.server.common.domain.KubeScanner
import ai.digital.integration.server.common.util.ProcessUtil
import net.sf.json.JSONObject
import net.sf.json.util.JSONTokener
import org.gradle.api.Project
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

class KubeScanningUtil {
    companion object {

        const val DEFAULT_RETRY_SLEEP_TIME: Int = 10
        const val DEFAULT_RETRY_TRIES: Int = 3

        private fun getKubeScanningDir(project: Project): String {
            return "${project.buildDir.toPath().toAbsolutePath()}/kube-scanning"
        }

        fun getKubeBenchDir(project: Project): String {
            return Paths.get("${getKubeScanningDir(project)}/kube-bench").toAbsolutePath().toString()
        }

        fun generateReport(project: Project, fileName: String) {
            ProcessUtil.executeCommand(project, "mkdir ${getKubeScanningReportDir(project).toAbsolutePath()}")
            ProcessUtil.executeCommand(project, "cd ${getKubeScanningReportDir(project).toAbsolutePath()}")
            val kubeBenchPod = ProcessUtil.executeCommand(project, "kubectl get po | awk '/kube-bench/{print \$1}'")
            var status: String
            var count = DEFAULT_RETRY_TRIES
            do {
                status = ProcessUtil.executeCommand(project, "kubectl get pods ${kubeBenchPod} -o 'jsonpath={..status.containerStatuses[0].state.terminated.reason}'")
                TimeUnit.SECONDS.sleep(DEFAULT_RETRY_SLEEP_TIME.toLong())
            } while (status != "Completed" && count-- > 0)

            val testReport: String = ProcessUtil.execute(project, "kubectl", listOf("logs", "pod/$kubeBenchPod"), true)
            File("${getKubeScanningReportDir(project).toAbsolutePath()}/$fileName").writeText(testReport)
        }

        private fun getKubeScanningReportDir(project: Project): Path {
            return Paths.get("${getKubeScanningDir(project)}/report")
        }

        fun getAWSAccountId(project: Project): String {
            val identityDetail: String = ProcessUtil.execute(project, "aws", listOf("sts", "get-caller-identity"), false)
            val identity = JSONTokener(identityDetail).nextValue() as JSONObject
            return "${identity.get("Account")}.dkr.ecr.${getKubeScanner(project).awsRegion}.amazonaws.com"
        }

        fun getKubeScanner(project: Project): KubeScanner {
            return DeployExtensionUtil.getExtension(project).kubeScanner.get()
        }

    }
}