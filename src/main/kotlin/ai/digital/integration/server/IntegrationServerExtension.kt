package ai.digital.integration.server

import ai.digital.integration.server.domain.*
import groovy.lang.Closure
import org.gradle.api.NamedDomainObjectContainer

open class IntegrationServerExtension(
    val clis: NamedDomainObjectContainer<Cli>,
    val databases: NamedDomainObjectContainer<Database>,
    val satellites: NamedDomainObjectContainer<Satellite>,
    val servers: NamedDomainObjectContainer<Server>,
    val tests: NamedDomainObjectContainer<Test>,
    val workers: NamedDomainObjectContainer<Worker>
) {

    var mqDriverVersions: Map<String, String> = mapOf()

    var xldIsDataVersion: String? = null

    fun clis(closure: Closure<NamedDomainObjectContainer<Cli>>) {
        clis.configure(closure)
    }

    fun databases(closure: Closure<NamedDomainObjectContainer<Database>>) {
        databases.configure(closure)
    }

    fun satellites(closure: Closure<NamedDomainObjectContainer<Satellite>>) {
        satellites.configure(closure)
    }

    fun servers(closure: Closure<NamedDomainObjectContainer<Server>>) {
        servers.configure(closure)
    }

    fun tests(closure: Closure<NamedDomainObjectContainer<Test>>) {
        tests.configure(closure)
    }

    fun workers(closure: Closure<NamedDomainObjectContainer<Worker>>) {
        workers.configure(closure)
    }
}
