package com.xebialabs.gradle.integration.util

import com.xebialabs.gradle.integration.IntegrationServerExtension
import com.xebialabs.gradle.integration.domain.Database
import com.xebialabs.gradle.integration.domain.Satellite
import com.xebialabs.gradle.integration.domain.Server
import com.xebialabs.gradle.integration.domain.Worker
import org.gradle.api.Project

class ExtensionUtil {
    static def EXTENSION_NAME = "integrationServer"

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
        project.extensions.create(EXTENSION_NAME,
                IntegrationServerExtension,
                project.container(Database),
                project.container(Satellite),
                project.container(Server),
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
