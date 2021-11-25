package ai.digital.integration.server.deploy.domain

import ai.digital.integration.server.common.constant.ServerConstants
import ai.digital.integration.server.common.util.HTTPUtil
import ai.digital.integration.server.deploy.internals.PermissionServiceUtil
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.property

open class Permission(objects: ObjectFactory) {
    var httpPort: Int = HTTPUtil.findFreePort()
    var dockerImage: String? = objects.property<String?>().orNull
    var stdoutFileName: String? = objects.property<String>().value("").get()
    var version: String? = objects.property<String?>().orNull
    var enabled: Boolean = objects.property<Boolean>().value(true).get()
    var pingRetrySleepTime: Int = ServerConstants.DEFAULT_PING_RETRY_SLEEP_TIME
    var pingTotalTries: Int = ServerConstants.PERMISSION_PING_TOTAL_TRIES
}
