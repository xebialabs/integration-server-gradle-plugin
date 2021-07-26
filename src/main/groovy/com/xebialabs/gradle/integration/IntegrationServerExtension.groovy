package com.xebialabs.gradle.integration

class IntegrationServerExtension {

    Integer akkaRemotingPort

    Integer configServerDebugPort

    Boolean configServerDebugSuspend

    String configServerVersion

    Integer configServerHttpPort

    Integer derbyPort

    Map<String, String> driverVersions

    Map<String, String> logLevels

    Boolean logSql

    Map<String, String> mqDriverVersions

    Map<String, List<Object>> overlays

    String provisionScript

    Integer provisionSocketTimeout

    Boolean removeStdoutConfig

    Integer satelliteDebugPort

    Boolean satelliteDebugSuspend

    Map<String, List<Object>> satelliteOverlays

    String satelliteVersion

    String serverContextRoot

    Integer serverDebugPort

    Boolean serverDebugSuspend

    String[] serverJvmArgs = []

    Integer serverHttpPort

    Integer serverPingRetrySleepTime

    Integer serverPingTotalTries

    String serverRuntimeDirectory

    String serverVersion

    String xldIsDataVersion

    Integer workerDebugPort

    Boolean workerDebugSuspend

    String[] workerJvmArgs = []

    String workerName

    Integer workerRemotingPort

    Map<String, Map<String, Object>> yamlPatches

    /** These 3 props has to be removed  */
    String anonymizerProvisionScript

    String ldapProvisionScript

    String oidcProvisionScript

}
