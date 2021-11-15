package ai.digital.integration.server.common.util

import ai.digital.integration.server.common.domain.Terraform
import ai.digital.integration.server.deploy.internals.DeployExtensionUtil
import org.gradle.api.Project

class TerraformUtil {
    companion object {
        fun execute(project: Project, args: List<String>, logOutput: Boolean = true): String {
            return ProcessUtil.execute(project, "terraform", args, logOutput)
        }

        fun getProvider(project: Project): String {
            return if(project.hasProperty("provider"))
                project.property("provider").toString()
            else
                "aws"
        }
    }
}