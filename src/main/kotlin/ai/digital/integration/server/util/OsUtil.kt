package ai.digital.integration.server.util

class OsUtil {
    companion object {
        @JvmStatic
        fun getPathSeparator() {
            val properties = System.getProperties()
            properties.getProperty("path.separator")
        }
    }
}
