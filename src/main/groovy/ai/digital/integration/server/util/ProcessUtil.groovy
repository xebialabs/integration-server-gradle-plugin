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

    static void exec(Map<String, Object> config) {
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

        def process = processBuilder.start()
        if (config.wait) {
            process.waitFor()
        }
    }

    static void chMod(Project project, String mode, String fileName) {
        project.exec {
            it.executable 'chmod'
            it.args "-R", mode, fileName
        }
    }
}