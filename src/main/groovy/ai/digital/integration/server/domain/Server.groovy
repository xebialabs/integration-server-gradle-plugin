package ai.digital.integration.server.domain

import ai.digital.integration.server.domain.api.Engine
import ai.digital.integration.server.util.HTTPUtil
import org.gradle.api.NamedDomainObjectContainer

class Server extends Engine {
    Map<String, String> copyBuildArtifacts = new HashMap<>()
    String contextRoot = "/"
    List<String> defaultOfficialPluginsToExclude = new LinkedList<>()
    NamedDomainObjectContainer<DevOpsAsCode> devOpsAsCodes
    String dockerImage
    Integer httpPort = HTTPUtil.findFreePort()
    List<String> generateDatasets = List.of()
    String name
    Integer pingRetrySleepTime = 10
    Integer pingTotalTries = 60
    Map<String, Map<String, Object>> yamlPatches = new HashMap<>()

    Server(final String name) {
        this.name = name
    }

    def devOpsAsCodes(final Closure closure) {
        devOpsAsCodes.configure(closure)
    }
}
