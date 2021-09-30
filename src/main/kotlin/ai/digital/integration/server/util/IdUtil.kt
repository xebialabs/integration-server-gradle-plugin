package ai.digital.integration.server.util

import org.apache.commons.lang.RandomStringUtils

const val SHORT_ID_LENGTH = 8

open class IdUtil {
    companion object {

        @JvmStatic
        fun shortId() {
            RandomStringUtils.random(SHORT_ID_LENGTH, "0123456789abcdef")
        }
    }
}
