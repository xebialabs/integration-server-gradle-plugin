package ai.digital.integration.server.common.domain

import ai.digital.integration.server.common.constant.ServerConstants
import ai.digital.integration.server.common.domain.api.Container
import ai.digital.integration.server.common.util.HTTPUtil
import org.gradle.api.model.ObjectFactory

@Suppress("UnstableApiUsage")
open class CentralConfigurationServer(objectFactory: ObjectFactory,
                                      name: String = "central-config-server") : Container(name) {
    var httpPort: Int? = HTTPUtil.findFreePort()
    var enabled: Boolean = false
    var pingRetrySleepTime: Int = ServerConstants.DEFAULT_PING_RETRY_SLEEP_TIME
    var pingTotalTries: Int = ServerConstants.DEFAULT_PING_TOTAL_TRIES
}
