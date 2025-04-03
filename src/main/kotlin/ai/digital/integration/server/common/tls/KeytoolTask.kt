package ai.digital.integration.server.common.tls

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.deploy.tasks.server.CentralConfigurationTask
import ai.digital.integration.server.deploy.tasks.server.ServerCopyOverlaysTask
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import org.gradle.process.ExecOperations
import org.gradle.process.ExecResult
import java.io.File
import java.util.*
import javax.inject.Inject

abstract class KeytoolTask @Inject constructor(
    private val execOperations: ExecOperations
): DefaultTask() {
    init {
        this.group = PluginConstant.PLUGIN_GROUP
        this.mustRunAfter(ServerCopyOverlaysTask.NAME)
        this.mustRunAfter(CentralConfigurationTask.NAME)

        this.doFirst {
            val customParams = ArrayList(params)
            val result = execTask(customParams, skipIfOutputFileExists())
            if (result != null && result.exitValue == 1) {
                throw RuntimeException("Running keytool with params: " + customParams.joinToString(" ") + " was not successfully executed.")
            }
        }
    }

    @Input
    var keyname: String? = null

    @InputDirectory
    @PathSensitive(PathSensitivity.ABSOLUTE)
    var workDir: File? = null

    @Input
    protected var params: List<String> = emptyList()

    @Input
    var keypass: String? = null

    @Input
    var storepass: String? = null

    @Input
    var type = "pkcs12"

    @Input
    var typeExtension = "p12"

    @OutputFile
    abstract fun getOutputFile(): File

    abstract fun skipIfOutputFileExists(): Boolean

    fun execTask(customParams: List<String?>, skipExec: Boolean): ExecResult? {
        val strings = ArrayList(customParams)
        if (keypass != null && type != "pkcs12") {
            strings.addAll(listOf("-keypass", keypass))
        }
        if (storepass != null) {
            strings.addAll(listOf("-storepass", storepass))
        }
        return if (skipExec && getOutputFile().exists()) {
            project.logger.lifecycle("Skipping keytool with args: " + strings.joinToString(" "))
            null
        } else {
            project.logger.lifecycle("Executing keytool with args: " + strings.joinToString(" "))
            execOperations.exec {
                executable("keytool")
                args(strings)
                workingDir = workDir
                isIgnoreExitValue = true
            }
        }
    }
}
