package ai.digital.integration.server.common.tls

import ai.digital.integration.server.deploy.internals.EntryPointUrlUtil.Companion.getHttpHost
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import java.io.File

@CacheableTask
open class KeytoolGenKeyTask : KeytoolTask() {

    companion object {
        const val NAME = "keytoolGenKey"
    }

    @Input
    var ip = "127.0.0.1"

    @Input
    var dns = getHttpHost()

    @Input
    var validity: String = 360.toString()

    @Input
    var keySize: String = 2048.toString()

    @OutputFile
    override fun getOutputFile(): File {
        return File(workDir!!.absolutePath + "/" + keyname + "." + typeExtension)
    }

    override fun skipIfOutputFileExists(): Boolean {
        return true
    }

    init {
        this.doFirst {
            params = listOf(
                "-genkey",
                "-alias",
                keyname!!,
                "-ext",
                "SAN:c=DNS:$dns,IP:$ip",
                "-dname",
                "CN=localhost,O=digital.ai,OU=Deploy",
                "-keyalg",
                "RSA",
                "-keystore",
                getOutputFile().absolutePath,
                "-storetype",
                type,
                "-validity",
                validity,
                "-keysize",
                keySize
            )
        }
    }
}
