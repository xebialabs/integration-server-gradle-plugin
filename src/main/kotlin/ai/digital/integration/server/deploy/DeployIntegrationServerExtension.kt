package ai.digital.integration.server.deploy

import ai.digital.integration.server.common.domain.*
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
    val project: Project,
    val satellites: NamedDomainObjectContainer<Satellite>,
    val servers: NamedDomainObjectContainer<Server>,
    val tests: NamedDomainObjectContainer<Test>,
    val workers: NamedDomainObjectContainer<Worker>
) {

    var mqDriverVersions: MutableMap<String, String> = mutableMapOf()

    var xldIsDataVersion: String? = null

    var tls: Tls? = null

    var akkaSecured: AkkaSecured? = null

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

    val clusterProfiles = project.objects.property<ClusterProfiles>().value(ClusterProfiles(project.objects))

    fun clusterProfiles(action: Action<in ClusterProfiles>) = action.execute(clusterProfiles.get())

    val cli = project.objects.property<Cli>().value(Cli(project.objects))

    fun cli(action: Action<in Cli>) = action.execute(cli.get())

    val cluster = project.objects.property<Cluster>().value(Cluster(project.objects))

    fun cluster(action: Action<in Cluster>) = action.execute(cluster.get())

    val database = project.objects.property<Database>().value(Database(project.objects))

    fun database(action: Action<in Database>) = action.execute(database.get())

    val maintenance = project.objects.property<Maintenance>().value(Maintenance(project.objects))

    fun maintenance(action: Action<in Maintenance>) = action.execute(maintenance.get())
}
