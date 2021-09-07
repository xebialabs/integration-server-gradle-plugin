package ai.digital.integration.server.domain

class Test {
    boolean base = false
    File baseDirectory
    Map<String, String> environments = Map.of()
    String excludesPattern = /\/(setup|teardown).py$/
    List<File> extraClassPath = List.of()
    String name
    String scriptPattern = /(.+)[.](py|cli)/
    List<String> setupScripts = List.of()
    Map<String, String> systemProperties = Map.of()
    List<String> tearDownScripts = List.of()

    Test(final String name) {
        this.name = name
    }
}
