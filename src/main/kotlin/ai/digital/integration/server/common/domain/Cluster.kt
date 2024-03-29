package ai.digital.integration.server.common.domain

import ai.digital.integration.server.common.constant.ClusterProfileName
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.property

@Suppress("UnstableApiUsage")
open class Cluster(objects: ObjectFactory) {
    var debugSuspend: Boolean = objects.property<Boolean>().value(false).get()
    var enable: Boolean = objects.property<Boolean>().value(false).get()
    var enableDebug: Boolean = objects.property<Boolean>().value(false).get()
    var profile: String = objects.property<String>().value(ClusterProfileName.DOCKER_COMPOSE.profileName).get()
    var publicPort: Int = objects.property<Int>().value(8080).get()
}
