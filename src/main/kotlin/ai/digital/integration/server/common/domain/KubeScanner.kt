package ai.digital.integration.server.common.domain

import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.property

@Suppress("UnstableApiUsage")
open class KubeScanner(objects: ObjectFactory) {

    @Input
    var enableDebug: Boolean = objects.property<Boolean>().value(false).get()

    @Input
    var awsRegion = objects.property<String?>().orNull

    @Input
    var logOutput = objects.property<Boolean>().value(false).get()

    @Input
    var kubeBenchTagVersion = objects.property<String?>().value("latest").get()

    @Input
    var command: MutableList<String> = objects.listProperty(String::class.java).value(mutableListOf<String>()).get()

}