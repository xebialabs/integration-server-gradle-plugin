package com.xebialabs.gradle.integration

class IntegrationServerExtension {
    Integer serverHttpPort

    Integer serverPingTotalTries

    Integer serverPingRetrySleepTime

    Integer provisionSocketTimeout

    Integer akkaRemotingPort

    Integer derbyPort

    Integer serverDebugPort

    String serverVersion

    String serverContextRoot

    Map<String, String> logLevels

    Map<String, List<Object>> overlays
}
