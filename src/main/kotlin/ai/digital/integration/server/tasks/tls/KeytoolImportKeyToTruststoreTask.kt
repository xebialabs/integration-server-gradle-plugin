package ai.digital.integration.server.tasks.tls

import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import java.io.File

@CacheableTask
open class KeytoolImportKeyToTruststoreTask : KeytoolTask() {

    companion object {
        @JvmStatic
        val NAME = "keytoolImportKeyToTruststore"
    }

    @Input
    var truststore: String? = null

    @InputFile
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
            this.params = listOf(
                    "-import", "-noprompt", "-alias", keyname!!, "-deststoretype", type,
                    "-file", getInputFile().absolutePath, "-keystore", getOutputFile().absolutePath
            )
        }
    }
}
