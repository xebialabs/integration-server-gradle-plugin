package ai.digital.integration.server.deploy.domain

import ai.digital.integration.server.deploy.internals.PermissionServiceUtil
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.property

open class Permission(objects: ObjectFactory) {
    var debugPort: Int? = null
    var debugSuspend: Boolean = false
    var dockerImage: String? = objects.property<String?>().orNull
    var stdoutFileName: String? = objects.property<String>().value("").get()
    var version: String? = objects.property<String?>().orNull
    var enabled: Boolean = objects.property<Boolean>().value(true).get()
    var runtimeDirectory: String? = null
}
