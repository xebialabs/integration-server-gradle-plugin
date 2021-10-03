package ai.digital.integration.server.domain

import ai.digital.integration.server.domain.api.Engine
import ai.digital.integration.server.util.HTTPUtil
import groovy.lang.Closure
import org.gradle.api.NamedDomainObjectContainer

open class Server(name: String) : Engine(name) {
    var copyBuildArtifacts = mutableMapOf<String, String>()
    var contextRoot: String = "/"
    var defaultOfficialPluginsToExclude: List<String> = mutableListOf()
    var dockerImage: String? = null
    var httpPort: Int = HTTPUtil.findFreePort()
    var generateDatasets: List<String> = mutableListOf()
    var pingRetrySleepTime: Int = 10
    var pingTotalTries: Int = 60
    var yamlPatches: Map<String, Map<String, Any>> = mutableMapOf()
    var devOpsAsCodes: NamedDomainObjectContainer<DevOpsAsCode>? = null
    var tls: Boolean = false
    var akkaSecured: Boolean = false

    fun devOpsAsCodes(closure: Closure<NamedDomainObjectContainer<DevOpsAsCode>>) {
        devOpsAsCodes?.configure(closure)
    }
}
