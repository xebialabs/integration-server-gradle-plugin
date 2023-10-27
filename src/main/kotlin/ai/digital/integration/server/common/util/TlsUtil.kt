package ai.digital.integration.server.common.util

import ai.digital.integration.server.common.domain.PekkoSecured
import ai.digital.integration.server.common.domain.Tls
import ai.digital.integration.server.deploy.internals.DeployExtensionUtil
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

        fun setPekkoSecured(project: Project, pekkoSecured: PekkoSecured?): PekkoSecured? {
            val ext = DeployExtensionUtil.getExtension(project)
            ext.pekkoSecured = pekkoSecured
            return pekkoSecured
        }

        fun getPekkoSecured(project: Project, workDir: String): PekkoSecured? {
            val ext = DeployExtensionUtil.getExtension(project)
            if (ext.pekkoSecured == null) {
                val pekkoSecured = PekkoSecured(workDir)
                setPekkoSecured(project, pekkoSecured)
            }
            return ext.pekkoSecured
        }

        fun generatePassword(name: String): String {
            return StringUtils.rightPad(StringUtils.substring(name, 0, 8), 8, "12345678")
        }
    }
}
