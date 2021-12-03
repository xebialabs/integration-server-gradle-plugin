package ai.digital.integration.server.common.util

import org.apache.tools.ant.taskdefs.condition.Os
import java.io.File

class XlCliUtil {
    companion object {
        fun download(version: String, location: File) {
            val osFolder = when {
                Os.isFamily(Os.FAMILY_WINDOWS) ->
                    "windows-amd64"
                Os.isFamily(Os.FAMILY_MAC) ->
                    "darwin-amd64"
                else ->
                    "linux-amd64"
            }

            ProcessUtil.executeCommand(
                    "wget https://dist.xebialabs.com/public/xl-cli/$version/$osFolder/xl", location)
            ProcessUtil.executeCommand("chmod +x xl", location)
        }

        fun xlApply(file: File, workDir: File) {
            ProcessUtil.executeCommand("./xl apply -v -f ${file.name}", workDir)
        }
    }
}
