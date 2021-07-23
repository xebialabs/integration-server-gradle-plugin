package com.xebialabs.gradle.integration

class IntegrationServerExtension {
    Integer satelliteDebugPort

    Boolean satelliteDebugSuspend

    Integer configServerDebugPort

    Boolean configServerDebugSuspend

    Integer configServerHttpPort

    Integer serverHttpPort

    Integer serverPingTotalTries

    Integer serverPingRetrySleepTime

    String[] serverJvmArgs =[]

    Integer provisionSocketTimeout

    Integer akkaRemotingPort

    Integer derbyPort

    Integer serverDebugPort

    Boolean serverDebugSuspend

    Boolean logSql

    String serverRuntimeDirectory

    String serverVersion

    String satelliteVersion

    String configServerVersion

    String serverContextRoot

    String xldIsDataVersion

    Boolean removeStdoutConfig

    Map<String, String> logLevels

    Map<String, List<Object>> overlays

    Map<String, List<Object>> satelliteOverlays

    Map<String, String> driverVersions

    Map<String, String> mqDriverVersions

    String workerName

    Integer workerRemotingPort

    Boolean workerDebugSuspend

    Integer workerDebugPort

    String[] workerJvmArgs = []

    String provisionScript

    String anonymizerProvisionScript

    String ldapProvisionScript

    String oidcProvisionScript

    Map<String, Map<String, Object>> yamlPatches

}
