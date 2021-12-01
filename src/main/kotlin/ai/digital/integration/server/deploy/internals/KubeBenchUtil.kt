package ai.digital.integration.server.deploy.internals

import ai.digital.integration.server.common.util.ProcessUtil
import org.gradle.api.Project
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

class KubeBenchUtil {
    companion object {

        fun getKubeBenchDir(project: Project): String {
            return Paths.get("${getBuildDir(project)}/kube-bench").toAbsolutePath().toString()
        }

        fun generateReport(project: Project, fileName: String) {
            ProcessUtil.executeCommand(project, "mkdir ${getKubeBenchReportDir(project).toAbsolutePath().toString()}")
            ProcessUtil.executeCommand(project, "cd ${getKubeBenchReportDir(project).toAbsolutePath().toString()}")
            val kubeBenchPod = ProcessUtil.executeCommand(project, "kubectl get po | awk '/kube-bench/{print \$1}'")
            var status: String
            do {
                status = ProcessUtil.executeCommand(project, "kubectl get pods ${kubeBenchPod} -o 'jsonpath={..status.containerStatuses[0].state.terminated.reason}'")
            } while (status != "Completed")

            val testReport: String = ProcessUtil.execute(project, "kubectl", listOf("logs", "pod/$kubeBenchPod"), true)
            File("${getKubeBenchReportDir(project).toAbsolutePath().toString()}/$fileName").writeText(testReport.toString())

        }


        fun getBuildDir(project: Project): String {
            return project.buildDir.toPath().toAbsolutePath().toString()
        }

        fun getKubeBenchReportDir(project: Project): Path {
            return Paths.get("${getBuildDir(project)}/kube-bench-report")
        }
    }
}