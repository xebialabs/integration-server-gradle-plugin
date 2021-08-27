package ai.digital.integration.server.tasks

import ai.digital.integration.server.domain.Test
import ai.digital.integration.server.util.CliUtil
import ai.digital.integration.server.util.ExtensionUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

class IntegrationTestsTask extends DefaultTask {
    static NAME = "integrationTests"

    IntegrationTestsTask() {
        this.configure {
            group = PLUGIN_GROUP
        }
    }

    static def findFiles(String basedir, String pattern) {
        new FileNameByRegexFinder().getFileNames(basedir, pattern).collect { new File(it) }
    }

    static def findFiles(String basedir, String pattern, String excludesPattern) {
        new FileNameByRegexFinder().getFileNames(basedir, pattern, excludesPattern).collect { new File(it) }
    }

    private def executeScripts(List<Test> tests) {
        project.logger.lifecycle("Executing test scripts ....")

        tests.each { Test test ->
            List<File> filesToExecute = new LinkedList<>()
            List<File> tearDownScripts = new LinkedList<>()

            def basedir = test.baseDirectory.absolutePath
            filesToExecute.addAll(findFiles(basedir, /\/${test.setupScript}$/))
            filesToExecute.addAll(findFiles(basedir, test.scriptPattern, test.excludesPattern))
            tearDownScripts.addAll(findFiles(basedir, /\/${test.tearDownScript}$/))

            try {
                filesToExecute.each { File source ->
                    CliUtil.executeScript(project, source, test.systemProperties)
                }
            } finally {
                tearDownScripts.each { File source ->
                    CliUtil.executeScript(project, source, test.systemProperties)
                }
            }
        }
    }

    @TaskAction
    void launch() {
        CliUtil.getCliLogFolder(project).deleteDir()
        executeScripts(ExtensionUtil.getExtension(project).tests.toList())
    }
}
