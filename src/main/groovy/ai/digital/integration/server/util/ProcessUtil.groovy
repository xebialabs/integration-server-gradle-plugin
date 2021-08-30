package ai.digital.integration.server.util

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project

class ProcessUtil {

    private static def createRunCommand(String baseCommand) {
        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            ["cmd" as String, "/c" as String, "${baseCommand}.cmd" as String]
        } else {
            ["./${baseCommand}.sh" as String]
        }
    }

    static void execAndCheck(Map<String, Object> config, File logFile) {
        if (exec(config).exitValue() == 1) {
            throw new RuntimeException("Running process was not successfully executed. Check logs [$logFile] for more information.")
        }
    }

    static Process exec(Map<String, Object> config) {
        def command = createRunCommand(config.command as String)
        if (config.params) {
            command.addAll(config.params as List<String>)
        }

        def processBuilder = new ProcessBuilder(command)
        if (config.environment) {
            processBuilder.environment().putAll(config.environment as Map<String, String>)
        }
        processBuilder.directory(config.workDir as File)
        if (config.inheritIO) {
            processBuilder.inheritIO()
        }

        if (config.discardIO) {
            processBuilder.redirectOutput(ProcessBuilder.Redirect.DISCARD)
            processBuilder.redirectError(ProcessBuilder.Redirect.DISCARD)
        }

        if (config.redirectTo) {
            processBuilder.redirectErrorStream(true)
            processBuilder.redirectOutput(ProcessBuilder.Redirect.to(config.redirectTo as File))
        }

        def process = processBuilder.start()
        if (config.wait) {
            process.waitFor()
        }

        process
    }

    static void chMod(Project project, String mode, String fileName) {
        project.exec {
            it.executable 'chmod'
            it.args "-R", mode, fileName
        }
    }
}
