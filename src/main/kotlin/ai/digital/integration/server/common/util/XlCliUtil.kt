package ai.digital.integration.server.common.util

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
import java.io.File

class XlCliUtil {
    companion object {
        fun download(project: Project, version: String, location: String) {
            val osFolder = when {
                Os.isFamily(Os.FAMILY_WINDOWS) ->
                    "windows-amd64"
                Os.isFamily(Os.FAMILY_MAC) ->
                    "darwin-amd64"
                else ->
                    "linux-amd64"
            }

            if (!ProcessUtil.executeCommand(project, "xl -v", logOutput = false, throwErrorOnFailure = false).contains("XL Cli")) {
                ProcessUtil.executeCommand(project,
                        "wget https://dist.xebialabs.com/public/xl-cli/$version/$osFolder/xl -P $location")
                ProcessUtil.executeCommand(project, "chmod +x xl", File(location))
            }
        }

        fun xlApply(project: Project, file: File, workDir: File) {
            if (ProcessUtil.executeCommand(project, "xl -v", logOutput = false, throwErrorOnFailure = false).contains("XL Cli")) {
                ProcessUtil.executeCommand(project, "xl apply -v -f ${File(workDir, file.name).absolutePath}")
            } else {
                ProcessUtil.executeCommand(project, "xl apply -v -f ${file.name}", workDir)
            }
        }
    }
}
