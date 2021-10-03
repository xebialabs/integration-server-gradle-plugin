package ai.digital.integration.server.util

class OsUtil {
    companion object {
        @JvmStatic
        fun getPathSeparator(): String {
            val properties = System.getProperties()
            return properties.getProperty("path.separator")
        }
    }
}
