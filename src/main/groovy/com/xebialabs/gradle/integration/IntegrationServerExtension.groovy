package com.xebialabs.gradle.integration

class IntegrationServerExtension {
    int serverHttpPort

    int serverPingTotalTries

    int serverPingRetrySleepTime

    int provisionSocketTimeout

    int akkaRemotingPort

    String serverVersion

    String serverContextRoot

    Map<String, String> logLevels

    Map<String, List<Object>> overlays
}
