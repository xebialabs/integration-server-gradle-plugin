package ai.digital.integration.server.common.util

import ai.digital.integration.server.common.constant.OperatorProviderName
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
import java.io.File

class XlCliUtil {
    companion object {

        val osFolder = when {
            Os.isFamily(Os.FAMILY_WINDOWS) ->
                "windows-amd64"
            Os.isFamily(Os.FAMILY_MAC) ->
                "darwin-amd64"
            else ->
                "linux-amd64"
        }

        fun distUrl(cliVersion: String) = "https://dist.xebialabs.com/public/xl-cli/$cliVersion/${osFolder}/xl"

        fun localDir(project: Project) = project.buildDir.resolve("xl-cli")

        val XL_OP_MAPPING = mapOf(
                Pair(OperatorProviderName.AWS_EKS, "AwsEKS"),
                Pair(OperatorProviderName.AZURE_AKS, "AzureAKS"),
                Pair(OperatorProviderName.GCP_GKE, "GoogleGKE"),
                Pair(OperatorProviderName.AWS_OPENSHIFT, "Openshift"),
                Pair(OperatorProviderName.ON_PREMISE, "PlainK8SCluster")
        )

        private fun copyFromLocal(project: Project, location: File) {
            val cliPath = localDir(project).resolve("xl")
            ProcessUtil.executeCommand(
                    "cp -f \"$cliPath\" \"$location\"", logOutput = false)
            ProcessUtil.executeCommand("chmod +x xl", location, logOutput = false)
        }

        fun xlApply(project: Project, file: File, workDir: File, deployServerForOperatorPort: Int) {
            copyFromLocal(project, workDir)
            ProcessUtil.executeCommand(project, "./xl apply --verbose -f \"${file.name}\" --xl-deploy-url http://localhost:${deployServerForOperatorPort}/ --xl-deploy-username admin --xl-deploy-password admin", workDir)
        }

        fun xlOp(project: Project, answersFile: File, workDir: File, blueprintPath: File?) {
            copyFromLocal(project, workDir)
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
