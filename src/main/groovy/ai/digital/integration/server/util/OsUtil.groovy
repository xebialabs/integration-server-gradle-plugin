package ai.digital.integration.server.util

class OsUtil {

    static def getPathSeparator() {
        Properties properties = System.getProperties()
        properties.getProperty("path.separator")
    }
}
