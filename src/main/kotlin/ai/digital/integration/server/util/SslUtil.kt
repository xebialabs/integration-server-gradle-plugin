package ai.digital.integration.server.util

import ai.digital.integration.server.IntegrationServerExtension
import ai.digital.integration.server.domain.AkkaSecured
import ai.digital.integration.server.domain.Tls
import org.apache.commons.lang3.StringUtils
import org.gradle.api.Project

class SslUtil {

    companion object {

        @JvmStatic
        fun setTls(project: Project, tls: Tls?): Tls? {
            val ext = project.extensions.getByType(IntegrationServerExtension::class.java)
            ext.tls = tls
            return tls
        }

        @JvmStatic
        fun getTls(project: Project, workDir: String): Tls? {
            val ext = project.extensions.getByType(IntegrationServerExtension::class.java)
            if (ext.tls == null) {
                val tls = Tls(workDir)
                setTls(project, tls)
            }
            return ext.tls
        }

        @JvmStatic
        fun setAkkaSecured(project: Project, akkaSecured: AkkaSecured?): AkkaSecured? {
            val ext = project.extensions.getByType(IntegrationServerExtension::class.java)
            ext.akkaSecured = akkaSecured
            return akkaSecured
        }

        @JvmStatic
        fun getAkkaSecured(project: Project, workDir: String): AkkaSecured? {
            val ext = project.extensions.getByType(IntegrationServerExtension::class.java)
            if (ext.akkaSecured == null) {
                val akkaSecured = AkkaSecured(workDir)
                setAkkaSecured(project, akkaSecured)
            }
            return ext.akkaSecured
        }

        @JvmStatic
        fun generatePassword(name: String): String {
            return StringUtils.rightPad(StringUtils.substring(name, 0, 8), 8, "12345678")
        }
    }
}
