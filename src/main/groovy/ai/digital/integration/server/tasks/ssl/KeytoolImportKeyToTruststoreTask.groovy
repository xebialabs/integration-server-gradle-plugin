package ai.digital.integration.server.tasks.ssl

import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.process.ExecResult

@CacheableTask
class KeytoolImportKeyToTruststoreTask extends KeytoolTask {
  static NAME = "keytoolImportKeyToTruststore"

  @Input
  String truststore

  @InputFile
  File getInputFile() {
    new File("${workDir.absolutePath}/${keyname}.cer")
  }

  @OutputFile
  File getOutputFile() {
    new File("${workDir.absolutePath}/${truststore}.$typeExtension")
  }

  @Override
  Boolean skipIfOutputFileExists() {
    String paramsString = "-list -alias $keyname -deststoretype $type -keystore ${getOutputFile()}"
    ExecResult result = execTask(paramsString.split().toList(), false)
    result.exitValue == 0
  }

  KeytoolImportKeyToTruststoreTask() {
    super()

    doFirst {
      String paramsString = "-import -noprompt -alias $keyname -deststoretype $type -file ${getInputFile()} -keystore ${getOutputFile()}"
      setParams(paramsString.split().toList())
    }
  }
}
