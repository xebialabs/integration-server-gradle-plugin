package ai.digital.integration.server.common.tls

import org.gradle.api.tasks.*
import java.io.File

@CacheableTask
open class KeytoolExportKeyToCertTask : KeytoolTask() {

    companion object {
        const val NAME = "keytoolExportKeyToCert"
    }

    @InputFile
    @PathSensitive(PathSensitivity.ABSOLUTE)
    fun getInputFile(): File {
        return File(workDir!!.absolutePath + "/" + keyname + "." + typeExtension)
    }

    @OutputFile
    override fun getOutputFile(): File {
        return File(workDir!!.absolutePath + "/" + keyname + ".cer")
    }

    override fun skipIfOutputFileExists(): Boolean {
        return false
    }

    init {
        this.doFirst {
            params = listOf("-export", "-keystore", getInputFile().absolutePath, "-alias", keyname!!, "-file", getOutputFile().absolutePath)
        }
    }
}
