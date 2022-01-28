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

        private fun download(cliUrl: String, location: File) {
            ProcessUtil.executeCommand(
                    "wget $cliUrl", location, logOutput = false)
            ProcessUtil.executeCommand("chmod +x xl", location, logOutput = false)
        }

        private fun copyFromLocal(cliPath: String, location: File) {
            ProcessUtil.executeCommand(
                    "cp $cliPath $location", logOutput = false)
            ProcessUtil.executeCommand("chmod +x xl", location, logOutput = false)
        }

        private fun checkIfXlDownloaded(location: File): Boolean = File(location.absolutePath, "xl").isFile

        private fun checkAndDownload(project: Project, cliPath: String, workDir: File) {
            if (cliPath.startsWith("http")) {
                if (!checkIfXlDownloaded(workDir)) {
                    download(cliPath, workDir)
                }
            } else {
                copyFromLocal(cliPath, workDir)
            }
        }

        fun getCliUrl(cliVersion: String): String {
            val osFolder = when {
                Os.isFamily(Os.FAMILY_WINDOWS) ->
                    "windows-amd64"
                Os.isFamily(Os.FAMILY_MAC) ->
                    "darwin-amd64"
                else ->
                    "linux-amd64"
            }
            return "https://dist.xebialabs.com/public/xl-cli/$cliVersion/$osFolder/xl"
        }

        fun xlApply(project: Project, cliPath: String, file: File, workDir: File, deployServerForOperatorPort: Int) {
            checkAndDownload(project, cliPath, workDir)
            ProcessUtil.executeCommand(project, "./xl apply --verbose -f \"${file.name}\" --xl-deploy-url http://localhost:${deployServerForOperatorPort}/ --xl-deploy-username admin --xl-deploy-password admin", workDir)
        }

        fun xlOp(project: Project, cliPath: String, answersFile: File, workDir: File, blueprintPath: File?) {
            checkAndDownload(project, cliPath, workDir)
            // hard coded container name "dai-deploy" is reserved for "xl op"
            DockerUtil.execute(project, arrayListOf("stop", "dai-deploy"), logOutput = false, throwErrorOnFailure = false)
            DockerUtil.execute(project, arrayListOf("rm", "dai-deploy"), logOutput = false, throwErrorOnFailure = false)

            val blueprintPathOption = if (blueprintPath != null) {
                "--local-repo \"${blueprintPath.absolutePath}\" "
            } else {
                ""
            }
            // dai-deploy is running on 4516, it is no possible to change that (for now)
            ProcessUtil.executeCommand(project, "./xl op --verbose --skip-prompts --no-cleanup --upgrade --advanced-setup " +
                    "--answers \"${answersFile.absolutePath}\" " +
                    "--xl-deploy-url http://localhost:4516/ --xl-deploy-username admin --xl-deploy-password admin $blueprintPathOption",
                    workDir = workDir)
        }
    }
}
