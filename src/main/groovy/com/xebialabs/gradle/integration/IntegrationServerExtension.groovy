package com.xebialabs.gradle.integration

class IntegrationServerExtension {
    Integer serverHttpPort

    Integer serverPingTotalTries

    Integer serverPingRetrySleepTime

    Integer provisionSocketTimeout

    Integer akkaRemotingPort

    Integer derbyPort

    Integer serverDebugPort

    Integer satelliteDebugPort

    Boolean serverDebugSuspend

    Boolean satelliteDebugSuspend

    Boolean logSql

    String serverVersion

    String serverContextRoot

    String xldIsDataVersion

    String xlSatelliteVersion

    Map<String, String> logLevels

    Map<String, List<Object>> overlays

    Map<String, String> driverVersions
}
