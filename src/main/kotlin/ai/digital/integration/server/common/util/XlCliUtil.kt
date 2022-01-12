package ai.digital.integration.server.common.util

import ai.digital.integration.server.common.constant.OperatorProviderName
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
import java.io.File

class XlCliUtil {
    companion object {

        val XL_OP_MAPPING = mapOf(
                Pair(OperatorProviderName.AWS_EKS, "AwsEKS"),
                Pair(OperatorProviderName.AZURE_AKS, "AzureAKS"),
                Pair(OperatorProviderName.GCP_GKE, "GoogleGKE"),
                Pair(OperatorProviderName.AWS_OPENSHIFT, "Openshift"),
                Pair(OperatorProviderName.ON_PREMISE, "PlainK8SCluster")
        )

        private fun download(version: String, location: File) {
            val osFolder = when {
                Os.isFamily(Os.FAMILY_WINDOWS) ->
                    "windows-amd64"
                Os.isFamily(Os.FAMILY_MAC) ->
                    "darwin-amd64"
                else ->
                    "linux-amd64"
            }

            ProcessUtil.executeCommand(
                    "wget https://dist.xebialabs.com/public/xl-cli/$version/$osFolder/xl", location, logOutput = false)
            ProcessUtil.executeCommand("chmod +x xl", location, logOutput = false)
        }

        private fun checkIfXlDownloaded(location: File): Boolean = File(location.absolutePath, "xl").isFile

        private fun checkAndDownload(version: String, workDir: File) {
            if (!checkIfXlDownloaded(workDir)) {
                download(version, workDir)
            }
        }

        fun xlApply(project: Project, file: File, version: String, workDir: File, deployServerForOperatorPort: Int) {
            checkAndDownload(version, workDir)
            ProcessUtil.executeCommand(project, "./xl apply --verbose -f \"${file.name}\" --xl-deploy-url http://localhost:${deployServerForOperatorPort}/ --xl-deploy-username admin --xl-deploy-password admin", workDir)
        }

        fun xlOp(project: Project, answersFile: File, version: String, workDir: File) {
            checkAndDownload(version, workDir)
            // hard coded container name "dai-deploy" is reserved for "xl op"
            DockerUtil.execute(project, arrayListOf("stop", "dai-deploy"), logOutput = false, throwErrorOnFailure = false)
            DockerUtil.execute(project, arrayListOf("rm", "dai-deploy"), logOutput = false, throwErrorOnFailure = false)
            // dai-deploy is running on 4516, it is no possible to change that (for now)
            ProcessUtil.executeCommand(project, "./xl op --verbose --skip-prompts --answers \"${answersFile.absolutePath}\" --xl-deploy-url http://localhost:4516/ --xl-deploy-username admin --xl-deploy-password admin --upgrade --advanced-setup",
                    workDir = workDir)
        }
    }
}
