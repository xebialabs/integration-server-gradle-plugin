package ai.digital.integration.server.common.domain

import ai.digital.integration.server.common.constant.ServerConstants
import ai.digital.integration.server.common.domain.api.Container
import ai.digital.integration.server.common.util.HTTPUtil
import groovy.lang.Closure
import org.gradle.api.NamedDomainObjectContainer
import java.io.File

open class Server(name: String) : Container(name) {
    var akkaSecured: Boolean = false
    var contextRoot: String = "/"
    var copyBuildArtifacts = mutableMapOf<String, String>()
    var copyFolders = mutableMapOf<String, List<File>>()
    var defaultOfficialPluginsToExclude: List<String> = mutableListOf()
    var devOpsAsCodes: NamedDomainObjectContainer<DevOpsAsCode>? = null
    var dockerImage: String? = null
    var centralConfigDockerImage: String? = null
    var httpPort: Int = HTTPUtil.findFreePort()
    var generateDatasets: List<String> = mutableListOf()
    var pingRetrySleepTime: Int = ServerConstants.DEFAULT_PING_RETRY_SLEEP_TIME
    var pingTotalTries: Int = ServerConstants.DEFAULT_PING_TOTAL_TRIES
    var previousInstallation: Boolean = false
    var tls: Boolean = false
    var yamlPatches: Map<String, Map<String, Any>> = mutableMapOf()

    fun devOpsAsCodes(closure: Closure<NamedDomainObjectContainer<DevOpsAsCode>>) {
        devOpsAsCodes?.configure(closure)
    }
}
