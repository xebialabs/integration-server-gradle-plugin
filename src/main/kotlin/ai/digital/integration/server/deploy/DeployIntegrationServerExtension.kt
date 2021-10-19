package ai.digital.integration.server.deploy

import ai.digital.integration.server.common.domain.*
import ai.digital.integration.server.deploy.domain.*
import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.kotlin.dsl.property

@Suppress("UnstableApiUsage")
open class DeployIntegrationServerExtension(
    val project: Project,
    val clis: NamedDomainObjectContainer<Cli>,
    val satellites: NamedDomainObjectContainer<Satellite>,
    val servers: NamedDomainObjectContainer<Server>,
    val tests: NamedDomainObjectContainer<Test>,
    val workers: NamedDomainObjectContainer<Worker>,
) {

    var mqDriverVersions: MutableMap<String, String> = mutableMapOf()

    var xldIsDataVersion: String? = null

    var tls: Tls? = null

    var akkaSecured: AkkaSecured? = null

    fun clis(closure: Closure<NamedDomainObjectContainer<Cli>>) {
        clis.configure(closure)
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

    val cli = project.objects.property<Database>().value(Database(project.objects))

    fun cli(action: Action<in Database>) = action.execute(database.get())

    val database = project.objects.property<Database>().value(Database(project.objects))

    fun database(action: Action<in Database>) = action.execute(database.get())
}
