package ai.digital.integration.server.common.util

import ai.digital.integration.server.deploy.DeployIntegrationServerExtension
import ai.digital.integration.server.common.domain.AkkaSecured
import ai.digital.integration.server.common.domain.Tls
import ai.digital.integration.server.deploy.util.DeployExtensionUtil
import org.apache.commons.lang3.StringUtils
import org.gradle.api.Project

class TlsUtil {

    companion object {

        fun setTls(project: Project, tls: Tls?): Tls? {
            DeployExtensionUtil.getExtension(project).tls = tls
            return tls
        }

        fun getTls(project: Project, workDir: String): Tls? {
            val ext = DeployExtensionUtil.getExtension(project)
            if (ext.tls == null) {
                val tls = Tls(workDir)
                setTls(project, tls)
            }
            return ext.tls
        }

        fun setAkkaSecured(project: Project, akkaSecured: AkkaSecured?): AkkaSecured? {
            val ext = DeployExtensionUtil.getExtension(project)
            ext.akkaSecured = akkaSecured
            return akkaSecured
        }

        fun getAkkaSecured(project: Project, workDir: String): AkkaSecured? {
            val ext = DeployExtensionUtil.getExtension(project)
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
