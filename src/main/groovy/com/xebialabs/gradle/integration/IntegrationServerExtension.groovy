package com.xebialabs.gradle.integration

import com.xebialabs.gradle.integration.domain.Database
import com.xebialabs.gradle.integration.domain.Satellite
import com.xebialabs.gradle.integration.domain.Server
import com.xebialabs.gradle.integration.domain.Worker
import org.gradle.api.NamedDomainObjectContainer

class IntegrationServerExtension {

    final NamedDomainObjectContainer<Database> databases

    final NamedDomainObjectContainer<Satellite> satellites

    final NamedDomainObjectContainer<Server> servers

    final NamedDomainObjectContainer<Worker> workers

    Map<String, String> mqDriverVersions = Map.of()

    String xldIsDataVersion

    IntegrationServerExtension(
            NamedDomainObjectContainer<Database> databases,
            NamedDomainObjectContainer<Satellite> satellites,
            NamedDomainObjectContainer<Server> servers,
            NamedDomainObjectContainer<Worker> workers) {
        this.databases = databases
        this.satellites = satellites
        this.servers = servers
        this.workers = workers
    }

    def databases(Closure closure) {
        databases.configure(closure)
    }

    def satellites(Closure closure) {
        satellites.configure(closure)
    }

    def servers(Closure closure) {
        servers.configure(closure)
    }

    def workers(Closure closure) {
        workers.configure(closure)
    }
}
