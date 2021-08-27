package ai.digital.integration.server.domain

class Test {
    File baseDirectory
    String name
    String scriptPattern = /(.+)[.](py|cli)/
    String excludesPattern = /\/(setup|teardown).py$/
    String setupScript = "setup.py"
    Map<String, String> systemProperties = Map.of()
    String tearDownScript = "teardown.py"

    Test(final String name) {
        this.name = name
    }
}
