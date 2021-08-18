package ai.digital.integration.server.domain

import ai.digital.integration.server.util.HTTPUtil
import org.gradle.api.NamedDomainObjectContainer

class Server {

    String contextRoot = "/"

    Boolean debugSuspend = false

    Integer debugPort

    String dockerImage

    Integer httpPort = HTTPUtil.findFreePort()

    String[] jvmArgs = ["-Xmx1024m", "-Duser.timezone=UTC"]

    Map<String, String> logLevels = new HashMap<>()

    String name

    List<String> defaultOfficialPluginsToExclude = new LinkedList<>()

    Map<String, List<Object>> overlays = new HashMap<>()

    Integer pingRetrySleepTime = 10

    Integer pingTotalTries = 60

    List<String> provisionScripts = List.of()

    List<String> generateDatasets = List.of()

    NamedDomainObjectContainer<DevOpsAsCode> devOpsAsCodes

    Integer provisionSocketTimeout = 60000 // TODO: add an impl for it

    Boolean removeStdoutConfig = false

    String runtimeDirectory

    String version

    Map<String, Map<String, Object>> yamlPatches = new HashMap<>()

    Server(final String name) {
        this.name = name
    }

    def devOpsAsCodes(final Closure closure) {
        devOpsAsCodes.configure(closure)
    }
}
