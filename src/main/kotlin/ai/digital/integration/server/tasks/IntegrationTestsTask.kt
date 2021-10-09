package ai.digital.integration.server.tasks

import ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.domain.Server
import ai.digital.integration.server.domain.Test
import ai.digital.integration.server.util.CliUtil
import ai.digital.integration.server.util.DeployServerUtil
import ai.digital.integration.server.util.FileUtil
import ai.digital.integration.server.util.TestUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class IntegrationTestsTask : DefaultTask() {

    companion object {
        @JvmStatic
        val NAME = "integrationTests"
    }

    init {
        this.group = PLUGIN_GROUP
    }

    private fun executeScripts(server: Server, tests: List<Test>) {
        project.logger.lifecycle("Executing test scripts ....")

        tests.forEach { test ->
            project.logger.lifecycle("About to execute test `${test.name}` ...")
            val filesToExecute = mutableListOf<File>()
            val filesForTeardown = mutableListOf<File>()

            test.baseDirectory?.let { dir ->
                val basedir = dir.absolutePath

                test.setupScripts.forEach { setupScript ->
                    filesToExecute.addAll(FileUtil.findFiles(basedir, "/${setupScript}$"))
                }

                filesToExecute.addAll(FileUtil.findFiles(basedir, test.scriptPattern, test.excludesPattern))

                test.tearDownScripts.forEach { teardownScript ->
                    filesToExecute.addAll(FileUtil.findFiles(basedir, "/${teardownScript}$"))
                    filesForTeardown.addAll(FileUtil.findFiles(basedir, "/${teardownScript}$"))
                }

                try {
                    CliUtil.executeScripts(project, filesToExecute, "test", server.tls, test)
                } catch (ignored: Exception) {
                    CliUtil.executeScripts(project, filesForTeardown, "teardown", server.tls, test)
                }
            }
        }
    }

    @TaskAction
    fun launch() {
        CliUtil.getCliLogFolder(project).deleteRecursively()
        executeScripts(DeployServerUtil.getServer(project), TestUtil.getExecutableTests(project))
    }
}
