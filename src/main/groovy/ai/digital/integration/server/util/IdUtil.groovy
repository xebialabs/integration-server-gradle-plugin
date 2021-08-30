package ai.digital.integration.server.util

import org.apache.commons.lang.RandomStringUtils

class IdUtil {

    static final int SHORT_ID_LENGTH = 8

    static String shortId() {
        RandomStringUtils.random(SHORT_ID_LENGTH, "0123456789abcdef")
    }
}
