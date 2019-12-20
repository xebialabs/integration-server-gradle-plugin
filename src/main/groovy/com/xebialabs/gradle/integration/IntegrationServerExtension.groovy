package com.xebialabs.gradle.integration

class IntegrationServerExtension {
    int serverHttpPort

    int serverPingTotalTries

    int serverPingRetrySleepTime

    int provisionSocketTimeout

    int akkaRemotingPort

    String serverVersion

    String serverContextRoot

    File serverLicense

    Map<String, String> logLevels

    List<Map<String, Object>> importArtifacts
}
