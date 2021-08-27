package ai.digital.integration.server

import ai.digital.integration.server.domain.*
import org.gradle.api.NamedDomainObjectContainer

class IntegrationServerExtension {

    final NamedDomainObjectContainer<Cli> clis

    final NamedDomainObjectContainer<Database> databases

    final NamedDomainObjectContainer<Satellite> satellites

    final NamedDomainObjectContainer<Server> servers

    final NamedDomainObjectContainer<Test> tests

    final NamedDomainObjectContainer<Worker> workers

    Map<String, String> mqDriverVersions = Map.of()

    String xldIsDataVersion

    IntegrationServerExtension(
            NamedDomainObjectContainer<Cli> clis,
            NamedDomainObjectContainer<Database> databases,
            NamedDomainObjectContainer<Satellite> satellites,
            NamedDomainObjectContainer<Server> servers,
            NamedDomainObjectContainer<Test> tests,
            NamedDomainObjectContainer<Worker> workers) {
        this.clis = clis
        this.databases = databases
        this.satellites = satellites
        this.servers = servers
        this.tests = tests
        this.workers = workers
    }

    def clis(Closure closure) {
        clis.configure(closure)
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

    def tests(Closure closure) {
        tests.configure(closure)
    }

    def workers(Closure closure) {
        workers.configure(closure)
    }
}
