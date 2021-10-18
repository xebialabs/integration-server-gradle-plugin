package ai.digital.integration.server.tasks.tls

import ai.digital.integration.server.util.DeployServerUtil.Companion.getHttpHost
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import java.io.File

@CacheableTask
open class KeytoolGenKeyTask : KeytoolTask() {

    companion object {
        @JvmStatic
        val NAME = "keytoolGenKey"
    }

    @Input
    private var ip = "127.0.0.1"

    @Input
    private var dns = getHttpHost()

    @Input
    private var validity: String = 360.toString()

    @Input
    private var keySize: String = 2048.toString()

    @OutputFile
    override fun getOutputFile(): File {
        return File(workDir!!.absolutePath + "/" + keyname + "." + typeExtension)
    }

    override fun skipIfOutputFileExists(): Boolean {
        return true
    }

    init {
        this.doFirst {
            this.params = listOf(
                    "-genkey", "-alias", keyname!!, "-ext", "SAN:c=DNS:$dns,IP:$ip", "-dname", "CN=localhost,O=digital.ai,OU=Deploy",
                    "-keyalg", "RSA", "-keystore", getOutputFile().absolutePath, "-storetype", type, "-validity", validity, "-keysize", keySize
            )
        }
    }
}
