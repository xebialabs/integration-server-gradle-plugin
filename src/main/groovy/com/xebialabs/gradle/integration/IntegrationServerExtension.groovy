package com.xebialabs.gradle.integration

class IntegrationServerExtension {
    Integer satelliteDebugPort

    Boolean satelliteDebugSuspend

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

    String satelliteVersion

    String serverContextRoot

    String xldIsDataVersion

    Boolean removeStdoutConfig

    Map<String, String> logLevels

    Map<String, List<Object>> overlays

    Map<String, List<Object>> satelliteOverlays

    Map<String, String> driverVersions

}
