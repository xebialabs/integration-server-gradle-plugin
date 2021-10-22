package ai.digital.integration.server.common.domain

import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.property

@Suppress("UnstableApiUsage")
open class Cluster(objects: ObjectFactory) {
    var publicPort: Int = objects.property<Int>().value(8080).get()
    var enable: Boolean = objects.property<Boolean>().value(false).get()
}
