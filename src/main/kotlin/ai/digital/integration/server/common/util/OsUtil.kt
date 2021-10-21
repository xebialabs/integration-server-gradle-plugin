package ai.digital.integration.server.common.util

class OsUtil {
    companion object {
        fun getPathSeparator(): String {
            val properties = System.getProperties()
            return properties.getProperty("path.separator")
        }
    }
}
