package ai.digital.integration.server.common.domain

import ai.digital.integration.server.common.constant.ServerConstants
import ai.digital.integration.server.common.domain.api.Container
import ai.digital.integration.server.common.util.HTTPUtil
import groovy.lang.Closure
import org.gradle.api.NamedDomainObjectContainer

open class Server(name: String) : Container(name) {
    var copyBuildArtifacts = mutableMapOf<String, String>()
    var contextRoot: String = "/"
    var defaultOfficialPluginsToExclude: List<String> = mutableListOf()
    var dockerImage: String? = null
    var httpPort: Int = HTTPUtil.findFreePort()
    var generateDatasets: List<String> = mutableListOf()
    var pingRetrySleepTime: Int = ServerConstants.DEFAULT_PING_RETRY_SLEEP_TIME
    var pingTotalTries: Int = ServerConstants.DEFAULT_PING_TOTAL_TRIES
    var yamlPatches: Map<String, Map<String, Any>> = mutableMapOf()
    var devOpsAsCodes: NamedDomainObjectContainer<DevOpsAsCode>? = null
    var tls: Boolean = false
    var akkaSecured: Boolean = false
    var previousInstallation: Boolean = false

    fun devOpsAsCodes(closure: Closure<NamedDomainObjectContainer<DevOpsAsCode>>) {
        devOpsAsCodes?.configure(closure)
    }
}
