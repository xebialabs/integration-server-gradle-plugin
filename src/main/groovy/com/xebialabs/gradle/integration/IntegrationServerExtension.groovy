package com.xebialabs.gradle.integration

class IntegrationServerExtension {
    Integer serverHttpPort

    Integer serverPingTotalTries

    Integer serverPingRetrySleepTime

    Integer provisionSocketTimeout

    Integer akkaRemotingPort

    Integer derbyPort

    Integer serverDebugPort

    Boolean serverDebugSuspend

    Boolean logSql

    String serverVersion

    String serverContextRoot

    String xldIsDataVersion

    Map<String, String> logLevels

    Map<String, List<Object>> overlays
}
