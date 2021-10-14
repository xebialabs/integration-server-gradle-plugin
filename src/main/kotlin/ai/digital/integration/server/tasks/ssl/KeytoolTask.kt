package ai.digital.integration.server.tasks.ssl

import ai.digital.integration.server.constant.PluginConstant
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.process.ExecResult
import java.io.File
import java.util.*

abstract class KeytoolTask: DefaultTask() {
    init {
        this.group = PluginConstant.PLUGIN_GROUP
        this.mustRunAfter("copyOverlays")
        this.mustRunAfter("centralConfiguration")

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
            project.exec {
                it.executable =  "keytool"
                it.args = strings
                it.workingDir = workDir
                it.isIgnoreExitValue = true
            }
        }
    }
}
