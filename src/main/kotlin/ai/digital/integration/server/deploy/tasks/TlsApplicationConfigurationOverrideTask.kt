package ai.digital.integration.server.deploy.tasks

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.domain.Tls
import ai.digital.integration.server.common.tls.KeytoolExportKeyToCertTask
import ai.digital.integration.server.common.tls.KeytoolGenKeyTask
import ai.digital.integration.server.common.tls.KeytoolImportKeyToTruststoreTask
import ai.digital.integration.server.deploy.util.DeployServerUtil.Companion.getServerWorkingDir
import ai.digital.integration.server.common.util.PropertiesUtil.Companion.readPropertiesFile
import ai.digital.integration.server.common.util.PropertiesUtil.Companion.writePropertiesFile
import ai.digital.integration.server.common.util.SslUtil.Companion.getTls
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.closureOf

open class TlsApplicationConfigurationOverrideTask : DefaultTask() {

    companion object {
        const val NAME = "tlsApplicationConfigurationOverride"
    }

    init {
        this.group = PluginConstant.PLUGIN_GROUP

        this.configure(closureOf<TlsApplicationConfigurationOverrideTask> {
            getTls(project, getServerWorkingDir(project))?.let { tls ->

                val genKeyStore = project.tasks.register("tls${KeytoolGenKeyTask.NAME.capitalize()}", KeytoolGenKeyTask::class.java) {
                    it.keyname = Tls.KEY_NAME
                    it.type = Tls.KEYSTORE_TYPE
                    it.typeExtension = Tls.KEYSTORE_TYPE_EXTENSION
                    it.workDir = tls.confWorkDir()
                    it.keypass = tls.keyPassword
                    it.storepass = tls.keyStorePassword
                }

                val genCert = project.tasks.register("tls${KeytoolExportKeyToCertTask.NAME.capitalize()}", KeytoolExportKeyToCertTask::class.java) {
                    it.keyname = Tls.KEY_NAME
                    it.type = Tls.KEYSTORE_TYPE
                    it.typeExtension = Tls.KEYSTORE_TYPE_EXTENSION
                    it.workDir = tls.confWorkDir()
                    it.keypass = tls.keyPassword
                    it.storepass = tls.keyStorePassword
                }
                project.tasks.getByName(genCert.name).dependsOn(genKeyStore)

                val genTrustStore = project.tasks.register("tls${KeytoolImportKeyToTruststoreTask.NAME.capitalize()}", KeytoolImportKeyToTruststoreTask::class.java) {
                    it.keyname = Tls.KEY_NAME
                    it.type = Tls.KEYSTORE_TYPE
                    it.typeExtension = Tls.KEYSTORE_TYPE_EXTENSION
                    it.truststore = tls.trustStoreName
                    it.workDir = tls.confWorkDir()
                    it.keypass = tls.keyPassword
                    it.storepass = tls.truststorePassword
                }
                project.tasks.getByName(genTrustStore.name).dependsOn(genCert)
                dependsOn(genKeyStore, genCert, genTrustStore)
            }
            mustRunAfter("copyOverlays")
        })
    }

    @TaskAction
    fun run() {
        val tls = getTls(project, getServerWorkingDir(project))
        updateDeployitConf(tls)
        updateWrapperConf(tls)
    }

    private fun updateDeployitConf(tls: Tls?) {
        project.logger.lifecycle("Configurations TLS overriding for deployit.conf.")
        val deployitConf = project.file("${tls!!.confWorkDir()}/deployit.conf")
        val properties = readPropertiesFile(deployitConf)
        properties["ssl"] = true.toString()
        properties["keystore.type"] = Tls.KEYSTORE_TYPE
        properties["keystore.password"] = tls.keyStorePassword
        if (Tls.KEYSTORE_TYPE != "pkcs12") {
            properties["keystore.keypassword"] = tls.keyPassword
        }
        properties["keystore.path"] = tls.keyStoreFile().absolutePath
        writePropertiesFile(deployitConf, properties)
    }

    private fun updateWrapperConf(tls: Tls?) {
        project.logger.lifecycle("Configurations TLS overriding for xld-wrapper.conf.common.")
        val wrapperConf = project.file(tls!!.confWorkDir().toString() + "/deployit.conf")
        val properties = readPropertiesFile(wrapperConf)
        var pos = 0
        while (pos < 20 && properties.containsKey("wrapper.java.additional.$pos")) {
            pos++
        }
        writePropertiesFile(wrapperConf, properties)
    }
}
