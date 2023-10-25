package ai.digital.integration.server.common.domain

import ai.digital.integration.server.common.util.TlsUtil
import java.io.File

class PekkoSecured(serverWorkingDir: String) {

    companion object {
        const val KEYSTORE_TYPE = "jks"
        const val KEYSTORE_TYPE_EXTENSION = "jks"
        const val MASTER_KEY_NAME = "pekko_ssl_master_"
        const val WORKER_KEY_NAME = "pekko_ssl_worker_"
        const val SATELLITE_KEY_NAME = "pekko_ssl_satellite_"
        const val TRUSTSTORE_NAME = "pekko_ssl_truststore_"
    }

    val trustStoreName: String = TRUSTSTORE_NAME
    val truststorePassword: String = TlsUtil.generatePassword(trustStoreName)
    private val confWorkDirPath: String = "$serverWorkingDir/conf"
    private val trustStoreFilePath: String = confWorkDir().toString() + "/" + trustStoreName + "." + KEYSTORE_TYPE_EXTENSION
    val keys: MutableMap<String, KeyMeta> = mutableMapOf()

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
            keyStorePassword = TlsUtil.generatePassword("s$keyStoreName")
            keyPassword = TlsUtil.generatePassword("k$keyStoreName")
            keyStoreFilePath = "$confWorkDir/$keyStoreName.$KEYSTORE_TYPE_EXTENSION"
        }
    }
}
