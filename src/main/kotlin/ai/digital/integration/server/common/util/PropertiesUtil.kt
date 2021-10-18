package ai.digital.integration.server.common.util

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

class PropertiesUtil {
    companion object {

        @JvmStatic
        fun readPropertiesFile(file: File): Properties {
            val properties = Properties()
            if (!file.exists()) {
                file.createNewFile()
            }
            val fis = FileInputStream(file.absolutePath)
            properties.load(fis)
            return properties
        }

        @JvmStatic
        fun readProperty(file: File, key: String): String {
            return readPropertiesFile(file).get(key).toString()
        }

        @JvmStatic
        fun writePropertiesFile(file: File, properties: Properties) {
            val fos = FileOutputStream(file.absolutePath)
            properties.store(fos, null)
        }
    }
}
