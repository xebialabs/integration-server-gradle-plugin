package ai.digital.integration.server.common.cluster.util

import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.domain.Server
import ai.digital.integration.server.common.util.FileUtil
import ai.digital.integration.server.common.util.IntegrationServerUtil
import ai.digital.integration.server.common.util.PropertiesUtil
import ai.digital.integration.server.common.util.WaitForBootUtil
import ai.digital.integration.server.deploy.internals.DeployExtensionUtil
import ai.digital.integration.server.deploy.internals.DeployServerUtil
import ai.digital.integration.server.deploy.internals.EntryPointUrlUtil
import ai.digital.integration.server.release.internals.ReleaseExtensionUtil
import ai.digital.integration.server.release.util.ReleaseServerUtil
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import java.io.File
import java.nio.file.Paths

class OperatorUtil(
    val project: Project
) {
    val productName =
        if (DeployExtensionUtil.getExtension(project).servers.size > 0) ProductName.DEPLOY else ProductName.RELEASE

    fun isClusterEnabled(): Boolean {
        return when (productName) {
            ProductName.DEPLOY -> DeployServerUtil.isClusterEnabled(project)
            ProductName.RELEASE -> ReleaseServerUtil.isClusterEnabled(project)
        }
    }

    fun getOperatorServer(): Server {
        val servers = when (productName) {
            ProductName.DEPLOY -> DeployExtensionUtil.getExtension(project).servers
            ProductName.RELEASE -> ReleaseExtensionUtil.getExtension(project).servers
        }

        fun findServer(servers: NamedDomainObjectContainer<Server>): Server {
            return servers.first { server ->
                !server.previousInstallation && server.dockerImage!!.endsWith("xl-deploy")
            }
        }

        return findServer(servers)
    }

    fun grantPermissionsToIntegrationServerFolder() {
        val workDir = IntegrationServerUtil.getDist(project)

        File(workDir).walk().forEach {
            FileUtil.grantRWPermissions(it)
        }
    }

    fun isDockerBased(): Boolean {
        return !DeployServerUtil.isDockerBased(getOperatorServer())
    }

    fun waitForBoot(server: Server) {
        fun saveLogs() {
            val name = ProductName.DEPLOY.toString().toLowerCase()
            DeployServerUtil.saveServerLogsToFile(project, server, "${name}-${server.version}")
        }

        val url = EntryPointUrlUtil(project, ProductName.DEPLOY, true)
            .composeUrl("/deployit/metadata/type", true)
        WaitForBootUtil.byPort(project, "Deploy", url, null, server.pingRetrySleepTime, server.pingTotalTries) {
            saveLogs()
        }
        saveLogs()
    }

    fun readConfProperty(key: String): String {
        val workdir = DeployServerUtil.getServerWorkingDir(project, getOperatorServer())
        val deployitConf = Paths.get("${workdir}/conf/deployit.conf").toFile()
        return PropertiesUtil.readProperty(deployitConf, key)
    }
}
