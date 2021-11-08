package ai.digital.integration.server.common.util

import org.gradle.api.Project

class TerraformUtil {
    companion object {
        fun execute(project: Project, args: List<String>, logOutput: Boolean = true): String {
            return ProcessUtil.execute(project, "terraform", args, logOutput)
        }
    }
}