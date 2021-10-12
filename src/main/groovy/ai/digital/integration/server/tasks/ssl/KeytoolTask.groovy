package ai.digital.integration.server.tasks.ssl

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.process.ExecResult

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

abstract class KeytoolTask extends DefaultTask {
  static NAME = "keytool"

  @Input
  String keyname

  @InputDirectory
  File workDir

  @Input
  List<String> params = []

  @Input
  String keypass

  @Input
  String storepass

  @Input
  String type = "pkcs12"

  @Input
  String typeExtension = "p12"

  abstract File getOutputFile()

  abstract Boolean skipIfOutputFileExists()

  KeytoolTask() {

    this.configure {
      group = PLUGIN_GROUP
    }

    doLast {
      def customParams = getParams().clone()
      def result = execTask(customParams, skipIfOutputFileExists())

      if (result != null && result.exitValue == 1) {
        throw new RuntimeException("Running keytool with params: ${customParams.join(" ")} was not successfully executed.")
      }
    }
  }

  ExecResult execTask(List<String> customParams, Boolean skipExec) {
    if (keypass && type != "pkcs12") {
      customParams += ["-keypass", keypass]
    }
    if (storepass) {
      customParams += ["-storepass", storepass]
    }

    if (skipExec && getOutputFile().exists()) {
      project.logger.lifecycle("Skipping keytool with args: ${customParams.join(" ")}")
      null
    } else {
      project.logger.lifecycle("Executing keytool with args: ${customParams.join(" ")}")

      project.exec {
        it.executable "keytool"
        it.args customParams
        it.workingDir workDir
        it.ignoreExitValue true
      }
    }
  }
}
