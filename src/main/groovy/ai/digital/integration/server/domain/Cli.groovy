package ai.digital.integration.server.domain

class Cli {
    Map<String, String> copyBuildArtifacts = new HashMap<>()
    boolean cleanDefaultExtContent = false
    Integer debugPort
    Boolean debugSuspend = false
    List<File> filesToExecute = List.of()
    String name
    Map<String, List<Object>> overlays = Map.of()
    Integer socketTimeout = 60000
    String version

    Cli(final String name) {
        this.name = name
    }
}
