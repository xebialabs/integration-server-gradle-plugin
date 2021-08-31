package ai.digital.integration.server.domain

class Test {
    File baseDirectory
    Map<String, String> environments = Map.of()
    List<File> extraClassPath = List.of()
    String name
    String scriptPattern = /(.+)[.](py|cli)/
    String excludesPattern = /\/(setup|teardown).py$/
    String setupScript
    Map<String, String> systemProperties = Map.of()
    String tearDownScript

    Test(final String name) {
        this.name = name
    }
}
