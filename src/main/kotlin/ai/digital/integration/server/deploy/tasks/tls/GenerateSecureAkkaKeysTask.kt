package ai.digital.integration.server.deploy.tasks.tls

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.domain.AkkaSecured
import ai.digital.integration.server.common.domain.AkkaSecured.KeyMeta
import ai.digital.integration.server.common.tls.KeytoolExportKeyToCertTask
import ai.digital.integration.server.common.tls.KeytoolGenKeyTask
import ai.digital.integration.server.common.tls.KeytoolImportKeyToTruststoreTask
import ai.digital.integration.server.deploy.internals.DeployServerUtil.Companion.getServer
import ai.digital.integration.server.deploy.internals.DeployServerUtil.Companion.getServerWorkingDir
import ai.digital.integration.server.deploy.internals.SatelliteUtil.Companion.getSatellites
import ai.digital.integration.server.common.util.TlsUtil.Companion.getAkkaSecured
import ai.digital.integration.server.deploy.internals.WorkerUtil.Companion.getWorkers
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.closureOf
import java.util.*

open class GenerateSecureAkkaKeysTask : DefaultTask() {

    companion object {
        const val NAME = "generateSecureAkkaKeys"
    }

    init {
        this.group = PluginConstant.PLUGIN_GROUP

        this.configure(closureOf<GenerateSecureAkkaKeysTask> {
            val server = getServer(project)
            val workers = getWorkers(project)
            val satellites = getSatellites(project)
            val masterName = AkkaSecured.MASTER_KEY_NAME + server.name
            getAkkaSecured(project, getServerWorkingDir(project))?.let { akkaSecured ->
                val masterKeyMeta = KeyMeta(akkaSecured.confWorkDir(), masterName)
                akkaSecured.keys[masterName] = masterKeyMeta

                val dependencies = generateKey(masterName, masterKeyMeta.keyPassword, masterKeyMeta.keyStorePassword, akkaSecured)

                dependencies.addAll(workers.map {
                    val name = AkkaSecured.WORKER_KEY_NAME + it.name
                    val keyMeta = KeyMeta(akkaSecured.confWorkDir(), name)
                    akkaSecured.keys[name] = masterKeyMeta
                    generateKey(name, keyMeta.keyPassword, keyMeta.keyStorePassword, akkaSecured)
                }.flatten())

                dependencies.addAll(satellites.map {
                    val name = AkkaSecured.SATELLITE_KEY_NAME + it.name
                    val keyMeta = KeyMeta(akkaSecured.confWorkDir(), name)
                    akkaSecured.keys[name] = masterKeyMeta
                    generateKey(name, keyMeta.keyPassword, keyMeta.keyStorePassword, akkaSecured)
                }.flatten())

                this.dependsOn(dependencies)
            }
        })
    }

    private fun generateKey(name: String, keyPassword: String, keyStorePassword: String, akkaSecured: AkkaSecured): MutableList<TaskProvider<*>> {
        val genKeyStore = project.tasks.register("akkaSecure${name.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}${KeytoolGenKeyTask.NAME.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}", KeytoolGenKeyTask::class.java) {
            keyname = name
            type = AkkaSecured.KEYSTORE_TYPE
            typeExtension = AkkaSecured.KEYSTORE_TYPE_EXTENSION
            workDir = akkaSecured.confWorkDir()
            keypass = keyPassword
            storepass = keyStorePassword
        }

        val genCert = project.tasks.register("akkaSecure${name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}${KeytoolExportKeyToCertTask.NAME.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}", KeytoolExportKeyToCertTask::class.java) {
            keyname = name
            type = AkkaSecured.KEYSTORE_TYPE
            typeExtension = AkkaSecured.KEYSTORE_TYPE_EXTENSION
            workDir = akkaSecured.confWorkDir()
            keypass = keyPassword
            storepass = keyStorePassword
        }
        project.tasks.getByName(genCert.name).dependsOn(genKeyStore)

        val genTrustStore = project.tasks.register("akkaSecure${name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}${KeytoolImportKeyToTruststoreTask.NAME.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}", KeytoolImportKeyToTruststoreTask::class.java) {
            keyname = name
            type = AkkaSecured.KEYSTORE_TYPE
            typeExtension = AkkaSecured.KEYSTORE_TYPE_EXTENSION
            truststore = akkaSecured.trustStoreName
            workDir = akkaSecured.confWorkDir()
            keypass = keyPassword
            storepass = akkaSecured.truststorePassword
        }
        project.tasks.getByName(genTrustStore.name).dependsOn(genCert)

        return mutableListOf(genKeyStore, genCert, genTrustStore)
    }
}
