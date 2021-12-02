package ai.digital.integration.server.common.domain

import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.property

@Suppress("UnstableApiUsage")
open class KubeScanner(objects: ObjectFactory) {

    var enableDebug: Boolean = objects.property<Boolean>().value(false).get()

    var awsRegion: String = objects.property<String>().value("us-east-1").get()
}