package ai.digital.integration.server.domain

class Test {
    File baseDirectory
    Map<String, String> environments = Map.of()
    List<File> extraClassPath = List.of()
    String name
    String scriptPattern = /(.+)[.](py|cli)/
    String excludesPattern = /\/(setup|teardown).py$/
    List<String> setupScripts = List.of()
    Map<String, String> systemProperties = Map.of()
    List<String> tearDownScripts = List.of()

    Test(final String name) {
        this.name = name
    }
}
