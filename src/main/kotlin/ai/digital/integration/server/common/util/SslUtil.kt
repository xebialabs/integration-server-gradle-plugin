package ai.digital.integration.server.common.util

import ai.digital.integration.server.deploy.DeployIntegrationServerExtension
import ai.digital.integration.server.common.domain.AkkaSecured
import ai.digital.integration.server.common.domain.Tls
import org.apache.commons.lang3.StringUtils
import org.gradle.api.Project

class SslUtil {

    companion object {

        fun setTls(project: Project, tls: Tls?): Tls? {
            val ext = project.extensions.getByType(DeployIntegrationServerExtension::class.java)
            ext.tls = tls
            return tls
        }

        fun getTls(project: Project, workDir: String): Tls? {
            val ext = project.extensions.getByType(DeployIntegrationServerExtension::class.java)
            if (ext.tls == null) {
                val tls = Tls(workDir)
                setTls(project, tls)
            }
            return ext.tls
        }

        fun setAkkaSecured(project: Project, akkaSecured: AkkaSecured?): AkkaSecured? {
            val ext = project.extensions.getByType(DeployIntegrationServerExtension::class.java)
            ext.akkaSecured = akkaSecured
            return akkaSecured
        }

        fun getAkkaSecured(project: Project, workDir: String): AkkaSecured? {
            val ext = project.extensions.getByType(DeployIntegrationServerExtension::class.java)
            if (ext.akkaSecured == null) {
                val akkaSecured = AkkaSecured(workDir)
                setAkkaSecured(project, akkaSecured)
            }
            return ext.akkaSecured
        }

        fun generatePassword(name: String): String {
            return StringUtils.rightPad(StringUtils.substring(name, 0, 8), 8, "12345678")
        }
    }
}
