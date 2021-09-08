package ai.digital.integration.server.domain

import ai.digital.integration.server.util.HTTPUtil
import org.gradle.api.NamedDomainObjectContainer

class Server {
    Map<String, String> copyBuildArtifacts = new HashMap<>()
    String contextRoot = "/"
    Boolean debugSuspend = false
    Integer debugPort
    List<String> defaultOfficialPluginsToExclude = new LinkedList<>()
    NamedDomainObjectContainer<DevOpsAsCode> devOpsAsCodes
    String dockerImage
    Integer httpPort = HTTPUtil.findFreePort()
    List<String> generateDatasets = List.of()
    String[] jvmArgs = ["-Xmx1024m", "-Duser.timezone=UTC"]
    Map<String, String> logLevels = new HashMap<>()
    String name
    Map<String, List<Object>> overlays = new HashMap<>()
    Integer pingRetrySleepTime = 10
    Integer pingTotalTries = 60
    String runtimeDirectory
    String stdoutFileNameForServerRuntime
    String version
    Map<String, Map<String, Object>> yamlPatches = new HashMap<>()

    Server(final String name) {
        this.name = name
    }

    def devOpsAsCodes(final Closure closure) {
        devOpsAsCodes.configure(closure)
    }
}
