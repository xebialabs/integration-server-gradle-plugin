package ai.digital.integration.server.tasks

import ai.digital.integration.server.domain.AkkaSecured
import ai.digital.integration.server.domain.Tls
import ai.digital.integration.server.tasks.ssl.KeytoolExportKeyToCertTask
import ai.digital.integration.server.tasks.ssl.KeytoolGenKeyTask
import ai.digital.integration.server.tasks.ssl.KeytoolImportKeyToTruststoreTask
import ai.digital.integration.server.util.DeployServerUtil
import ai.digital.integration.server.util.SatelliteUtil
import ai.digital.integration.server.util.ServerUtil
import ai.digital.integration.server.util.SslUtil
import ai.digital.integration.server.util.WorkerUtil
import org.gradle.api.DefaultTask

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

class GenerateSecureAkkaKeysTask extends DefaultTask {

  static NAME = "generateSecureAkkaKeys"

  GenerateSecureAkkaKeysTask() {

    this.configure { ->
      group = PLUGIN_GROUP

      def server = DeployServerUtil.getServer(project)
      def workers = WorkerUtil.getWorkers(project)
      def satellites = SatelliteUtil.getSatellites(project)

      def akkaSecured = SslUtil.getAkkaSecured(project,  DeployServerUtil.getServerWorkingDir(project))

      def masterName = "${AkkaSecured.MASTER_KEY_NAME}${server.name}".toString()
      def masterKeyMeta = new AkkaSecured.KeyMeta(akkaSecured.confWorkDir(), masterName)
      akkaSecured.keys.put(masterName, masterKeyMeta)

      def dependencies = generateKey(masterName, masterKeyMeta.keyPassword, masterKeyMeta.keyStorePassword, akkaSecured)

      dependencies += workers.collect {
        def name = "${AkkaSecured.WORKER_KEY_NAME}${it.name}".toString()
        def keyMeta = new AkkaSecured.KeyMeta(akkaSecured.confWorkDir(), name)
        akkaSecured.keys.put(name, masterKeyMeta)
        generateKey(name, keyMeta.keyPassword, keyMeta.keyStorePassword, akkaSecured)
      }.flatten()

      dependencies += satellites.collect {
        def name = "${AkkaSecured.SATELLITE_KEY_NAME}${it.name}".toString()
        def keyMeta = new AkkaSecured.KeyMeta(akkaSecured.confWorkDir(), name)
        akkaSecured.keys.put(name, masterKeyMeta)
        generateKey(name, keyMeta.keyPassword, keyMeta.keyStorePassword, akkaSecured)
      }.flatten()

      this.dependsOn dependencies
    }
  }

  List generateKey(String name, String keyPassword, String keyStorePassword, AkkaSecured akkaSecured) {

    def genKeyStore = project.getTasks().register("akkaSecure${name.capitalize()}${KeytoolGenKeyTask.NAME.capitalize()}", KeytoolGenKeyTask.class) {
      keyname = name
      type = AkkaSecured.KEYSTORE_TYPE
      typeExtension = AkkaSecured.KEYSTORE_TYPE_EXTENSION
      workDir = akkaSecured.confWorkDir()
      keypass = keyPassword
      storepass = keyStorePassword
    }

    def genCert = project.getTasks().register("akkaSecure${name.capitalize()}${KeytoolExportKeyToCertTask.NAME.capitalize()}", KeytoolExportKeyToCertTask.class) {
      keyname = name
      type = AkkaSecured.KEYSTORE_TYPE
      typeExtension = AkkaSecured.KEYSTORE_TYPE_EXTENSION
      workDir = akkaSecured.confWorkDir()
      keypass = keyPassword
      storepass = keyStorePassword
    }
    project.tasks.getByName(genCert.name).dependsOn genKeyStore

    def genTrustStore = project.getTasks().register("akkaSecure${name.capitalize()}${KeytoolImportKeyToTruststoreTask.NAME.capitalize()}", KeytoolImportKeyToTruststoreTask.class) {
      keyname = name
      type = AkkaSecured.KEYSTORE_TYPE
      typeExtension = AkkaSecured.KEYSTORE_TYPE_EXTENSION
      truststore = akkaSecured.getTrustStoreName()
      workDir = akkaSecured.confWorkDir()
      keypass = keyPassword
      storepass = akkaSecured.getTruststorePassword()
    }
    project.tasks.getByName(genTrustStore.name).dependsOn genCert

    [genKeyStore, genCert, genTrustStore]
  }
}
