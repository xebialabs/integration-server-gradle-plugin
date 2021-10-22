package ai.digital.integration.server.common.domain

import ai.digital.integration.server.common.util.TlsUtil
import java.io.File

class Tls(serverWorkingDir: String) {

    companion object {
        const val KEYSTORE_TYPE = "pkcs12"
        const val KEYSTORE_TYPE_EXTENSION = "pk12"
        const val KEY_NAME = "master_tls"
        const val TRUSTSTORE_SUFFIX_NAME = "truststore"
    }

   var keyStorePassword: String = TlsUtil.generatePassword("s$KEY_NAME")
   var keyPassword: String = TlsUtil.generatePassword("k$KEY_NAME")
   var truststorePassword: String = TlsUtil.generatePassword("t$KEY_NAME")
   var trustStoreName: String = "$KEY_NAME-$TRUSTSTORE_SUFFIX_NAME"
   var confWorkDirPath: String = "$serverWorkingDir/conf"
   var keyStoreFilePath: String = confWorkDir().toString() + "/" + KEY_NAME + "." + KEYSTORE_TYPE_EXTENSION
   var trustStoreFilePath: String = confWorkDir().toString() + "/" + trustStoreName + "." + KEYSTORE_TYPE_EXTENSION

    fun confWorkDir(): File {
        return File(confWorkDirPath)
    }

    fun keyStoreFile(): File {
        return File(keyStoreFilePath)
    }

    fun trustStoreFile(): File {
        return File(trustStoreFilePath)
    }
}
