package ai.digital.integration.server.domain

class Satellite {
    Integer akkaStreamingPort = 8480
    Integer debugPort
    Boolean debugSuspend = false
    String name
    Integer metricsPort = 8080
    Integer serverAkkaPort = 8380
    String serverAkkaHostname = "127.0.0.1"
    String serverAkkaBindHostName = "0.0.0.0"
    Map<String, List<Object>> overlays = Map.of()
    String version

    Satellite(final String name) {
        this.name = name
    }
}
