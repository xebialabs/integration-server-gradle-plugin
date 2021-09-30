package ai.digital.integration.server.tasks

import ai.digital.integration.server.domain.Test
import ai.digital.integration.server.util.CliUtil
import ai.digital.integration.server.util.FileUtil
import ai.digital.integration.server.util.TestUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

class IntegrationTestsTask extends DefaultTask {
    public static String NAME = "integrationTests"

    IntegrationTestsTask() {
        this.configure {
            group = PLUGIN_GROUP
        }
    }

    private def executeScripts(List<Test> tests) {
        project.logger.lifecycle("Executing test scripts ....")

        tests.each { Test test ->
            project.logger.lifecycle("About to execute test `${test.name}` ...")
            List<File> filesToExecute = new LinkedList<>()
            List<File> filesForTeardown = new LinkedList<>()

            if (test.baseDirectory.exists()) {
                String basedir = test.baseDirectory.absolutePath

                test.setupScripts.each { setupScript ->
                    filesToExecute.addAll(FileUtil.findFiles(basedir, /\/${setupScript}$/))
                }

                filesToExecute.addAll(FileUtil.findFiles(basedir, test.scriptPattern, test.excludesPattern))

                test.tearDownScripts.each { teardownScript ->
                    filesToExecute.addAll(FileUtil.findFiles(basedir, /\/${teardownScript}$/))
                    filesForTeardown.addAll(FileUtil.findFiles(basedir, /\/${teardownScript}$/))
                }

                try {
                    CliUtil.executeScripts(project, filesToExecute, "test", test)
                } catch (Exception ignored) {
                    CliUtil.executeScripts(project, filesForTeardown, "teardown", test)
                }
            } else {
                project.logger.lifecycle("Base directory ${test.baseDirectory.absolutePath} doesn't exist. Execution of test `${test.name}` has been skipped.")
            }
        }
    }

    @TaskAction
    void launch() {
        CliUtil.getCliLogFolder(project).deleteDir()
        executeScripts(TestUtil.getExecutableTests(project))
    }
}
