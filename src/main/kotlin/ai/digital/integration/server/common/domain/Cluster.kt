package ai.digital.integration.server.common.domain

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property

@Suppress("UnstableApiUsage")
open class Cluster(objects: ObjectFactory) {
    var publicPort: Property<Int> = objects.property<Int>().value(8080)
    var enable: Property<Boolean> = objects.property<Boolean>().value(false)
}
