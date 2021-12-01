package ai.digital.integration.server.common.util

import org.gradle.api.Project
import java.io.File

class XlCliUtil {
    companion object {
        fun download(version: String, location: String) {
            ProcessUtil.executeCommand(
                "curl -LO https://dist.xebialabs.com/public/xl-cli/$version/linux-amd64/xl > $location/xl")
            ProcessUtil.executeCommand("chmod +x $location/xl")
        }

        fun xlApply(project: Project, file: File, workdir: File): String {
            val dir = IntegrationServerUtil.getDist(project)

            return ProcessUtil.executeCommand("xl apply -v -f ${file.absolutePath}", workdir)
        }

    }
}
