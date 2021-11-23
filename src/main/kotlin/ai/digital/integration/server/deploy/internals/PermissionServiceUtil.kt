package ai.digital.integration.server.deploy.internals

import ai.digital.integration.server.common.util.IntegrationServerUtil
import ai.digital.integration.server.common.util.PropertyUtil
import ai.digital.integration.server.deploy.domain.Permission
import org.gradle.api.Project
import java.io.File
import java.nio.file.Paths
import kotlin.system.exitProcess

class PermissionServiceUtil {
    companion object {
        fun hasPermissionService(project: Project): Boolean {
            return getPermissionService(project).enabled
        }

        fun getPermissionService(project: Project): Permission {
            val permission = DeployExtensionUtil.getExtension(project).permissions.get()
            return enrichPermissionService(project, permission)
        }

        private fun enrichPermissionService(project: Project, permission: Permission): Permission {
            permission.debugPort = getDebugPort(project, permission)
            permission.version = getPermissionServiceVersion(project, permission)
            permission.dockerImage = permission.dockerImage
            permission.enabled = false
            return permission
        }

        private fun getPermissionServiceWorkingDir(project: Project, permission: Permission): String {
            val targetDir = IntegrationServerUtil.getDist(project)
            return Paths.get(targetDir, "deploy-permission-service-${permission.version}").toAbsolutePath()
                    .toString()
        }

        fun getPermissionServiceWorkingDir(project: Project): String {
            return getPermissionServiceWorkingDir(project, getPermissionService(project))
        }

        fun getBinDir(project: Project, permission: Permission): File {
            return Paths.get(getPermissionServiceWorkingDir(project, permission), "bin").toFile()
        }

        fun getPermissionServiceLogDir(project: Project, permission: Permission): File {
            return project.file("${getPermissionServiceWorkingDir(project, permission)}/log")
        }

        fun getPermissionServiceLog(project: Project, permission: Permission): File {
            return project.file("${getPermissionServiceLogDir(project, permission)}/deploy-permission.log")
        }

        fun getSatelliteConf(project: Project, satellite: Permission): File {
            return project.file("${getPermissionServiceWorkingDir(project, satellite)}/conf/satellite.conf")
        }

        fun isDockerBased(project: Project): Boolean {
            return !getPermissionService(project).dockerImage.isNullOrBlank()
        }

        private fun getPermissionServiceVersion(project: Project, permission: Permission): String? {
            return when {
                project.hasProperty("deployPermissionServiceVersion") -> {
                    project.property("deployPermissionServiceVersion").toString()
                }
                !permission.version.isNullOrEmpty() -> {
                    permission.version
                }
                else -> {
                    project.logger.warn("Permissions Service Version is not specified, skipping...")
                    ""
                }
            }
        }

        private fun getDebugPort(project: Project, permission: Permission): Int? {
            return if (PropertyUtil.resolveBooleanValue(project, "debug", true)) {
                PropertyUtil.resolveIntValue(project, "permissionServiceDebugPort", permission.debugPort)
            } else {
                null
            }
        }
    }
}
