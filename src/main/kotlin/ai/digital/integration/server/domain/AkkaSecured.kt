package ai.digital.integration.server.domain

import ai.digital.integration.server.util.SslUtil
import java.io.File

class AkkaSecured(serverWorkingDir: String) {

    companion object {
        const val KEYSTORE_TYPE = "jks"
        const val KEYSTORE_TYPE_EXTENSION = "jks"
        const val MASTER_KEY_NAME = "akka_ssl_master_"
        const val WORKER_KEY_NAME = "akka_ssl_worker_"
        const val SATELLITE_KEY_NAME = "akka_ssl_satellite_"
        const val TRUSTSTORE_NAME = "akka_ssl_truststore_"
    }

    val trustStoreName: String = TRUSTSTORE_NAME
    val truststorePassword: String = SslUtil.generatePassword(trustStoreName)
    private val confWorkDirPath: String = "$serverWorkingDir/conf"
    private val trustStoreFilePath: String = confWorkDir().toString() + "/" + trustStoreName + "." + KEYSTORE_TYPE_EXTENSION
    val keys: Map<String?, KeyMeta?> = LinkedHashMap()

    fun confWorkDir(): File {
        return File(confWorkDirPath)
    }

    fun trustStoreFile(): File {
        return File(trustStoreFilePath)
    }

    class KeyMeta(confWorkDir: File, keyStoreName: String) {
        fun keyStoreFile(): File {
            return File(keyStoreFilePath)
        }

        var keyStorePassword: String
        var keyPassword: String
        var keyStoreFilePath: String

        init {
            keyStorePassword = SslUtil.generatePassword("s$keyStoreName")
            keyPassword = SslUtil.generatePassword("k$keyStoreName")
            keyStoreFilePath = "$confWorkDir/$keyStoreName.$KEYSTORE_TYPE_EXTENSION"
        }
    }
}
