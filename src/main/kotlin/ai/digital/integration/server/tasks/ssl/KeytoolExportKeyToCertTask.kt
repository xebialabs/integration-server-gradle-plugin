package ai.digital.integration.server.tasks.ssl

import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import java.io.File

@CacheableTask
open class KeytoolExportKeyToCertTask : KeytoolTask() {

    companion object {
        @JvmStatic
        val NAME = "keytoolExportKeyToCert"
    }

    @InputFile
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
            this.params = listOf("-export", "-keystore", getInputFile().absolutePath, "-alias", keyname!!, "-file", getOutputFile().absolutePath)
        }
    }
}
