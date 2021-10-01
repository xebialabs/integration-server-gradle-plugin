package ai.digital.integration.server.domain

import ai.digital.integration.server.util.SslUtil
import java.io.File

class Tls(serverWorkingDir: String) {

    companion object {
        val KEYSTORE_TYPE = "pkcs12"
        val KEYSTORE_TYPE_EXTENSION = "pk12"
        val KEY_NAME = "master_tls"
        val TRUSTSTORE_SUFFIX_NAME = "truststore"
    }

   var keyStorePassword: String = SslUtil.generatePassword("s$KEY_NAME")
   var keyPassword: String = SslUtil.generatePassword("k$KEY_NAME")
   var truststorePassword: String = SslUtil.generatePassword("t$KEY_NAME")
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
