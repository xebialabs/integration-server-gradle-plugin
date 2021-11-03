package ai.digital.integration.server.common.domain

import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.property

@Suppress("UnstableApiUsage")
open class Cluster(objects: ObjectFactory) {
    var debugSuspend: Boolean = objects.property<Boolean>().value(false).get()
    var enable: Boolean = objects.property<Boolean>().value(false).get()
    var enableDebug: Boolean = objects.property<Boolean>().value(false).get()
    var enableDatabaseLoadBalancer: Boolean = objects.property<Boolean>().value(false).get()
    var publicPort: Int = objects.property<Int>().value(8080).get()
}
