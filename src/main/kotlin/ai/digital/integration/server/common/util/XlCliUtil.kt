package ai.digital.integration.server.common.util

import org.gradle.api.Project
import java.io.File

class XlCliUtil {
    companion object {

        fun apply(project: Project, file: File): String {
            return ProcessUtil.executeCommand(project,
                    "xl apply -v -f ${file.absolutePath}")
        }

    }
}
