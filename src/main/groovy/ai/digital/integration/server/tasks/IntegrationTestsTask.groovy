package ai.digital.integration.server.tasks

import ai.digital.integration.server.domain.Test
import ai.digital.integration.server.util.CliUtil
import ai.digital.integration.server.util.ExtensionUtil
import ai.digital.integration.server.util.FileUtil
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

    def getTestScriptPattern(Test test) {
        project.hasProperty("testScriptPattern") ? project.getProperty("testScriptPattern") : test.scriptPattern
    }

    private def executeScripts(List<Test> tests) {
        project.logger.lifecycle("Executing test scripts ....")

        tests.each { Test test ->
            project.logger.lifecycle("About to execute test `${test.name}` ...")
            List<File> filesToExecute = new LinkedList<>()
            List<File> tearDownScripts = new LinkedList<>()

            if (test.baseDirectory.exists()) {
                String basedir = test.baseDirectory.absolutePath

                if (test.setupScript?.trim()) {
                    filesToExecute.addAll(FileUtil.findFiles(basedir, /\/${test.setupScript}$/))
                }
                filesToExecute.addAll(FileUtil.findFiles(basedir, getTestScriptPattern(test), test.excludesPattern))
                if (test.tearDownScript?.trim()) {
                    tearDownScripts.addAll(FileUtil.findFiles(basedir, /\/${test.tearDownScript}$/))
                }

                try {
                    filesToExecute.each { File source ->
                        CliUtil.executeScript(project, source, test)
                    }
                } finally {
                    tearDownScripts.each { File source ->
                        CliUtil.executeScript(project, source, test)
                    }
                }
            } else {
                project.logger.lifecycle("Base directory ${test.baseDirectory.absolutePath} doesn't exist. Execution of test `${test.name}` has been skipped.")
            }
        }
    }

    @TaskAction
    void launch() {
        CliUtil.getCliLogFolder(project).deleteDir()
        executeScripts(ExtensionUtil.getExtension(project).tests.toList())
    }
}
