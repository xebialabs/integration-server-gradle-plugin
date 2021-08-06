package com.xebialabs.gradle.integration

import org.gradle.api.NamedDomainObjectContainer

class IntegrationServerExtension {

    final NamedDomainObjectContainer<Worker> workers

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

    Map<String, Map<String, Object>> yamlPatches

    IntegrationServerExtension(NamedDomainObjectContainer<Worker> workers) {
        this.workers = workers
    }

    def workers(Closure closure) {
        workers.configure(closure)
    }
}
