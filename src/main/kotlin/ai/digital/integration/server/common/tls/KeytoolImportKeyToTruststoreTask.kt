package ai.digital.integration.server.common.tls

import org.gradle.api.tasks.*
import org.gradle.process.ExecOperations
import java.io.File

@CacheableTask
open class KeytoolImportKeyToTruststoreTask(execOperations: ExecOperations) : KeytoolTask(execOperations) {

    companion object {
        const val NAME = "keytoolImportKeyToTruststore"
    }

    @Input
    var truststore: String? = null

    @InputFile
    @PathSensitive(PathSensitivity.ABSOLUTE)
    fun getInputFile(): File {
        return File(workDir!!.absolutePath + "/" + keyname + ".cer")
    }

    @OutputFile
    override fun getOutputFile(): File {
        return File(workDir!!.absolutePath + "/" + truststore + "." + typeExtension)
    }

    override fun skipIfOutputFileExists(): Boolean {
        val params = listOf("-list", "-alias", keyname, "-deststoretype", type, "-keystore", getOutputFile().absolutePath)
        val result = execTask(params, false)
        return result!!.exitValue == 0
    }

    init {
        this.doFirst {
            params = listOf(
                    "-import", "-noprompt", "-alias", keyname!!, "-deststoretype", type,
                    "-file", getInputFile().absolutePath, "-keystore", getOutputFile().absolutePath
            )
        }
    }
}
