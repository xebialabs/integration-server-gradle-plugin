package ai.digital.integration.server.util

import ai.digital.integration.server.IntegrationServerExtension
import ai.digital.integration.server.domain.*
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project

class ExtensionUtil {
    static def IS_EXTENSION_NAME = "integrationServer"

    private static def resolveValue(Project project, IntegrationServerExtension extension, String propertyName, def defaultValue) {
        if (project.hasProperty(propertyName)) {
            project.property(propertyName)
        } else {
            def propertyValue = extension[propertyName]
            propertyValue ? propertyValue : defaultValue
        }
    }

    static IntegrationServerExtension getExtension(Project project) {
        project.extensions.getByType(IntegrationServerExtension)
    }

    static createExtension(Project project) {

        final NamedDomainObjectContainer<Server> servers =
                project.container(Server)

        servers.all {
            devOpsAsCodes = project.container(DevOpsAsCode)
        }

        project.extensions.create(IS_EXTENSION_NAME,
                IntegrationServerExtension,
                project.container(Cli),
                project.container(Database),
                project.container(Satellite),
                servers,
                project.container(Test),
                project.container(Worker)
        )
    }

    static initialize(Project project) {
        def extension = getExtension(project)
        extension.xldIsDataVersion = resolveValue(project, extension, "xldIsDataVersion",
                project.hasProperty("xldIsDataVersion") ? project.property("xldIsDataVersion") : null)
        extension.mqDriverVersions = resolveValue(project, extension, "mqDriverVersions", [
                'activemq': '5.16.2',
                'rabbitmq': '2.2.0'
        ])
    }
}
