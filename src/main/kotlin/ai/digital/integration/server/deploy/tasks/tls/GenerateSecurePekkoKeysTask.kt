package ai.digital.integration.server.deploy.tasks.tls

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.domain.PekkoSecured
import ai.digital.integration.server.common.domain.PekkoSecured.KeyMeta
import ai.digital.integration.server.common.tls.KeytoolExportKeyToCertTask
import ai.digital.integration.server.common.tls.KeytoolGenKeyTask
import ai.digital.integration.server.common.tls.KeytoolImportKeyToTruststoreTask
import ai.digital.integration.server.deploy.internals.DeployServerUtil.Companion.getServer
import ai.digital.integration.server.deploy.internals.DeployServerUtil.Companion.getServerWorkingDir
import ai.digital.integration.server.deploy.internals.SatelliteUtil.Companion.getSatellites
import ai.digital.integration.server.common.util.TlsUtil.Companion.getPekkoSecured
import ai.digital.integration.server.deploy.internals.WorkerUtil.Companion.getWorkers
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.closureOf
import java.util.*

open class GenerateSecurePekkoKeysTask : DefaultTask() {

    companion object {
        const val NAME = "generateSecurePekkoKeys"
    }

    init {
        this.group = PluginConstant.PLUGIN_GROUP

        this.configure(closureOf<GenerateSecurePekkoKeysTask> {
            val server = getServer(project)
            val workers = getWorkers(project)
            val satellites = getSatellites(project)
            val masterName = PekkoSecured.MASTER_KEY_NAME + server.name
            getPekkoSecured(project, getServerWorkingDir(project))?.let { pekkoSecured ->
                val masterKeyMeta = KeyMeta(pekkoSecured.confWorkDir(), masterName)
                pekkoSecured.keys[masterName] = masterKeyMeta

                val dependencies = generateKey(masterName, masterKeyMeta.keyPassword, masterKeyMeta.keyStorePassword, pekkoSecured)

                dependencies.addAll(workers.map {
                    val name = PekkoSecured.WORKER_KEY_NAME + it.name
                    val keyMeta = KeyMeta(pekkoSecured.confWorkDir(), name)
                    pekkoSecured.keys[name] = masterKeyMeta
                    generateKey(name, keyMeta.keyPassword, keyMeta.keyStorePassword, pekkoSecured)
                }.flatten())

                dependencies.addAll(satellites.map {
                    val name = PekkoSecured.SATELLITE_KEY_NAME + it.name
                    val keyMeta = KeyMeta(pekkoSecured.confWorkDir(), name)
                    pekkoSecured.keys[name] = masterKeyMeta
                    generateKey(name, keyMeta.keyPassword, keyMeta.keyStorePassword, pekkoSecured)
                }.flatten())

                this.dependsOn(dependencies)
            }
        })
    }

    private fun generateKey(name: String, keyPassword: String, keyStorePassword: String, pekkoSecured: PekkoSecured): MutableList<TaskProvider<*>> {
        val genKeyStore = project.tasks.register("pekkoSecure${name.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}${KeytoolGenKeyTask.NAME.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}", KeytoolGenKeyTask::class.java) {
            keyname = name
            type = PekkoSecured.KEYSTORE_TYPE
            typeExtension = PekkoSecured.KEYSTORE_TYPE_EXTENSION
            workDir = pekkoSecured.confWorkDir()
            keypass = keyPassword
            storepass = keyStorePassword
        }

        val genCert = project.tasks.register("pekkoSecure${name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}${KeytoolExportKeyToCertTask.NAME.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}", KeytoolExportKeyToCertTask::class.java) {
            keyname = name
            type = PekkoSecured.KEYSTORE_TYPE
            typeExtension = PekkoSecured.KEYSTORE_TYPE_EXTENSION
            workDir = pekkoSecured.confWorkDir()
            keypass = keyPassword
            storepass = keyStorePassword
        }
        project.tasks.getByName(genCert.name).dependsOn(genKeyStore)

        val genTrustStore = project.tasks.register("pekkoSecure${name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}${KeytoolImportKeyToTruststoreTask.NAME.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}", KeytoolImportKeyToTruststoreTask::class.java) {
            keyname = name
            type = PekkoSecured.KEYSTORE_TYPE
            typeExtension = PekkoSecured.KEYSTORE_TYPE_EXTENSION
            truststore = pekkoSecured.trustStoreName
            workDir = pekkoSecured.confWorkDir()
            keypass = keyPassword
            storepass = pekkoSecured.truststorePassword
        }
        project.tasks.getByName(genTrustStore.name).dependsOn(genCert)

        return mutableListOf(genKeyStore, genCert, genTrustStore)
    }
}
