package ai.digital.integration.server.domain

class Satellite {
    Integer debugPort
    Boolean debugSuspend = false
    String name
    Map<String, List<Object>> overlays = Map.of()
    String version

    Satellite(final String name) {
        this.name = name
    }
}
