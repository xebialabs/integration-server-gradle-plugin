package ai.digital.integration.server.tasks.ssl

import ai.digital.integration.server.util.DeployServerUtil
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile

@CacheableTask
class KeytoolGenKeyTask extends KeytoolTask {
  static NAME = "keytoolGenKey"

  @Input
  String ip = "127.0.0.1"

  @Input
  String dns = DeployServerUtil.getHttpHost()

  @Input
  String validity = 360

  @Input
  String keySize = 2048

  @OutputFile
  File getOutputFile() {
    new File("${workDir.absolutePath}/${keyname}.$typeExtension")
  }

  @Override
  Boolean skipIfOutputFileExists() {
    true
  }

  KeytoolGenKeyTask() {
    super()

    doFirst {
      String paramsString = "-genkey -alias $keyname -ext SAN:c=DNS:$dns,IP:$ip " +
          "-dname CN=localhost,O=digital.ai,OU=Deploy -keyalg RSA " +
          "-keystore ${getOutputFile()} " +
          "-storetype $type -validity $validity -keysize $keySize"
      setParams(paramsString.split().toList())
    }
  }
}
