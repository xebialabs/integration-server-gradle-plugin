package ai.digital.integration.server.domain

import ai.digital.integration.server.domain.api.Engine
import ai.digital.integration.server.util.HTTPUtil
import groovy.lang.Closure
import org.gradle.api.NamedDomainObjectContainer

open class Server(name: String) : Engine(name) {
    var copyBuildArtifacts = mapOf<String, String>()
    var contextRoot: String = "/"
    var defaultOfficialPluginsToExclude: List<String> = listOf()
    var dockerImage: String? = null
    var httpPort: Int = HTTPUtil.findFreePort()
    var generateDatasets: List<String> = listOf()
    var pingRetrySleepTime: Int = 10
    var pingTotalTries: Int = 10
    var yamlPatches: Map<String, Map<String, Any>> = mapOf()
    var devOpsAsCodes: NamedDomainObjectContainer<DevOpsAsCode>? = null

    fun devOpsAsCodes(closure: Closure<NamedDomainObjectContainer<DevOpsAsCode>>) {
        devOpsAsCodes?.configure(closure)
    }
}
