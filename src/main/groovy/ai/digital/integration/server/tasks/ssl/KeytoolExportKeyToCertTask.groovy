package ai.digital.integration.server.tasks.ssl

import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile

@CacheableTask
class KeytoolExportKeyToCertTask extends KeytoolTask {
  static NAME = "keytoolExportKeyToCert"

  @InputFile
  File getInputFile() {
    new File("${workDir.absolutePath}/${keyname}.${typeExtension}")
  }

  @OutputFile
  File getOutputFile() {
    new File("${workDir.absolutePath}/${keyname}.cer")
  }

  @Override
  Boolean skipIfOutputFileExists() {
    false
  }

  KeytoolExportKeyToCertTask() {
    super()

    doFirst {
      def params = [
          "-export", "-keystore", getInputFile().absolutePath, "-alias",  keyname,
          "-file", getOutputFile().absolutePath
      ]
      setParams(params)
    }
  }

}
