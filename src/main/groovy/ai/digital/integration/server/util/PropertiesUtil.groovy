package ai.digital.integration.server.util

class PropertiesUtil {

    static Properties readPropertiesFile(File file) {
        Properties properties = new Properties()
        if (!file.exists()) {
            file.createNewFile()
        }
        file.withInputStream {
            properties.load(it)
        }
        properties
    }

    static void writePropertiesFile(File file, Properties properties) {
        properties.store(file.newWriter(), null)
    }
}
