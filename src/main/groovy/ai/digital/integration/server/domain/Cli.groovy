package ai.digital.integration.server.domain

class Cli {

    Integer debugPort

    Boolean debugSuspend = false

    List<File> filesToExecute = List.of()

    String name

    Map<String, List<Object>> overlays = Map.of()

    String version

    Integer cliSocketTimeout = 60000

    Cli(final String name) {
        this.name = name
    }
}
