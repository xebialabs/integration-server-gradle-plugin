package ai.digital.integration.server.deploy.domain

import ai.digital.integration.server.common.util.HTTPUtil
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.property

@Suppress("UnstableApiUsage")
open class CentralConfigurationStandalone(objects: ObjectFactory) {
    var httpPort: Int? = HTTPUtil.findFreePort()
    var debugPort: Int? = objects.property<Int?>().orNull
    var debugSuspend: Boolean = objects.property<Boolean>().value(false).get()
    var enable: Boolean = objects.property<Boolean>().value(false).get()
    var version: String? = null
    var stdoutFileName: String? = null
    var dockerImage: String? = null
}
