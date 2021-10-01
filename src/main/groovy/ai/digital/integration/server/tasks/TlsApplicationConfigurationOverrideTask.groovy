package ai.digital.integration.server.tasks

import ai.digital.integration.server.domain.Tls
import ai.digital.integration.server.tasks.ssl.KeytoolExportKeyToCertTask
import ai.digital.integration.server.tasks.ssl.KeytoolGenKeyTask
import ai.digital.integration.server.tasks.ssl.KeytoolImportKeyToTruststoreTask
import ai.digital.integration.server.util.PropertiesUtil
import ai.digital.integration.server.util.ServerUtil
import ai.digital.integration.server.util.SslUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

class TlsApplicationConfigurationOverrideTask extends DefaultTask {

  static NAME = "tlsApplicationConfigurationOverride"

  TlsApplicationConfigurationOverrideTask() {

    this.configure { ->
      group = PLUGIN_GROUP
      mustRunAfter CopyOverlaysTask.NAME, ApplicationConfigurationOverrideTask.NAME

      def tls = SslUtil.getTls(project, ServerUtil.getServerWorkingDir(project))

      def genKeyStore = project.getTasks().register("tls${KeytoolGenKeyTask.NAME.capitalize()}", KeytoolGenKeyTask.class) {
        keyname = Tls.KEY_NAME
        type = Tls.KEYSTORE_TYPE
        typeExtension = Tls.KEYSTORE_TYPE_EXTENSION
        workDir = tls.confWorkDir()
        keypass = tls.getKeyPassword()
        storepass = tls.getKeyStorePassword()
      }

      def genCert = project.getTasks().register("tls${KeytoolExportKeyToCertTask.NAME.capitalize()}", KeytoolExportKeyToCertTask.class) {
        keyname = Tls.KEY_NAME
        type = Tls.KEYSTORE_TYPE
        typeExtension = Tls.KEYSTORE_TYPE_EXTENSION
        workDir = tls.confWorkDir()
        keypass = tls.getKeyPassword()
        storepass = tls.getKeyStorePassword()
      }
      project.tasks.getByName(genCert.name).dependsOn genKeyStore

      def genTrustStore = project.getTasks().register("tls${KeytoolImportKeyToTruststoreTask.NAME.capitalize()}", KeytoolImportKeyToTruststoreTask.class) {
        keyname = Tls.KEY_NAME
        type = Tls.KEYSTORE_TYPE
        typeExtension = Tls.KEYSTORE_TYPE_EXTENSION
        truststore = tls.getTrustStoreName()
        workDir = tls.confWorkDir()
        keypass = tls.getKeyPassword()
        storepass = tls.getTruststorePassword()
      }
      project.tasks.getByName(genTrustStore.name).dependsOn genCert

      this.dependsOn genKeyStore, genCert, genTrustStore
    }
  }

  @TaskAction
  def run() {
    def tls = SslUtil.getTls(project, ServerUtil.getServerWorkingDir(project))
    updateDeployitConf(tls)
    updateWrapperConf(tls)
  }

  def updateDeployitConf(Tls tls) {
    project.logger.lifecycle("Configurations TLS overriding for deployit.conf.")

    def deployitConf = project.file("${tls.confWorkDir()}/deployit.conf")

    def properties = PropertiesUtil.readPropertiesFile(deployitConf)
    properties.put("ssl", true.toString())
    properties.put("keystore.type", Tls.KEYSTORE_TYPE)
    properties.put("keystore.password", tls.keyStorePassword)
    if (Tls.KEYSTORE_TYPE != "pkcs12") {
      properties.put("keystore.keypassword", tls.keyPassword)
    }
    properties.put("keystore.path", tls.keyStoreFile().absolutePath)

    PropertiesUtil.writePropertiesFile(deployitConf, properties)
  }

  def updateWrapperConf(Tls tls) {
    project.logger.lifecycle("Configurations TLS overriding for xld-wrapper.conf.common.")

    def wrapperConf = project.file("${tls.confWorkDir()}/deployit.conf")

    def properties = PropertiesUtil.readPropertiesFile(wrapperConf)

    int pos = 0
    while (pos < 20 && properties.containsKey("wrapper.java.additional.$pos")) {
      pos++
    }

    PropertiesUtil.writePropertiesFile(wrapperConf, properties)
  }
}
