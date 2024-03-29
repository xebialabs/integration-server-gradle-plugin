package ai.digital.integration.server.deploy

import ai.digital.integration.server.common.domain.*
import ai.digital.integration.server.common.extension.CommonIntegrationServerExtension
import ai.digital.integration.server.deploy.domain.CentralConfigurationServer
import ai.digital.integration.server.deploy.domain.Cli
import ai.digital.integration.server.deploy.domain.Satellite
import ai.digital.integration.server.deploy.domain.Worker
import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.kotlin.dsl.property


@Suppress("UnstableApiUsage")
open class DeployIntegrationServerExtension(
    project: Project,
    val satellites: NamedDomainObjectContainer<Satellite>,
    val servers: NamedDomainObjectContainer<Server>,
    val tests: NamedDomainObjectContainer<Test>,
    val workers: NamedDomainObjectContainer<Worker>,
    val infrastructures: NamedDomainObjectContainer<Infrastructure>
) : CommonIntegrationServerExtension(project) {

    var mqDriverVersions: MutableMap<String, String> = mutableMapOf()

    var xldIsDataVersion: String? = null

    var tls: Tls? = null

    var pekkoSecured: PekkoSecured? = null

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

    fun infrastructures(closure: Closure<NamedDomainObjectContainer<Infrastructure>>){
        infrastructures.configure(closure)
    }

    val cli = project.objects.property<Cli>().value(Cli(project.objects))

    fun cli(action: Action<in Cli>) = action.execute(cli.get())

    val database = project.objects.property<Database>().value(Database(project.objects))

    fun database(action: Action<in Database>) = action.execute(database.get())

    val maintenance = project.objects.property<Maintenance>().value(Maintenance(project.objects))

    fun maintenance(action: Action<in Maintenance>) = action.execute(maintenance.get())

    val kubeScanner = project.objects.property<KubeScanner>().value(KubeScanner(project.objects))

    fun kubeScanner(action: Action<in KubeScanner>) = action.execute(kubeScanner.get())

    val centralConfigurationServer = project.objects.property<CentralConfigurationServer>().value(CentralConfigurationServer(project.objects))

    fun centralConfigurationServer(action: Action<in CentralConfigurationServer>) = action.execute(centralConfigurationServer.get())
}
