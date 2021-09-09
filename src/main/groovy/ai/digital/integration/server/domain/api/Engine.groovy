package ai.digital.integration.server.domain.api

class Engine {
    Integer debugPort
    Boolean debugSuspend = false
    String[] jvmArgs = ["-Xmx1024m", "-Duser.timezone=UTC"]
    Map<String, String> logLevels = new HashMap<>()
    Map<String, List<Object>> overlays = new HashMap<>()
    String runtimeDirectory
    String version
    String stdoutFileName
}
