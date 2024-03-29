package ai.digital.integration.server.common.util

import ai.digital.integration.server.common.domain.Test
import ai.digital.integration.server.deploy.internals.DeployExtensionUtil
import org.gradle.api.Project
import java.io.File

class TestUtil {
    companion object {
        fun getTestBaseDirectory(project: Project, test: Test): File? {

            if (project.hasProperty("testBaseSubDirectory")) {
                return File(test.baseDirectory, project.property("testBaseSubDirectory").toString())
            }

            getBaseTest(project)?.let { t ->
                t.baseDirectory?.let { base ->
                    return base
                }
            }

            return test.baseDirectory
        }

        fun getTestEnvironments(project: Project, test: Test): Map<String, String> {
            getBaseTest(project)?.let { it ->
                return it.environments
            }
            return test.environments
        }

        fun getTestExcludePattern(project: Project, test: Test): String {
            getBaseTest(project)?.let { base ->
                return base.excludesPattern
            }
            return test.excludesPattern
        }

        fun getTestExtraClassPath(project: Project, test: Test): List<File> {
            getBaseTest(project)?.let { base ->
                return base.extraClassPath
            }
            return test.extraClassPath
        }

        fun getTestSystemProperties(project: Project, test: Test): Map<String, String> {
            getBaseTest(project)?.let { base ->
                return base.systemProperties
            }
            return test.systemProperties
        }

        fun getTestScriptPattern(project: Project, test: Test): String {
            if (project.hasProperty("testScriptPattern")) {
                return project.property("testScriptPattern").toString()
            }

            getBaseTest(project)?.let { base ->
                return base.scriptPattern
            }

            return test.scriptPattern
        }

        fun getTestSetupScripts(project: Project, test: Test): List<String> {
            if (project.hasProperty("testSetupScripts")) {
                return project.property("testSetupScripts").toString().split(",")
            }

            getBaseTest(project)?.let { base ->
                return base.setupScripts
            }

            return test.setupScripts
        }

        fun getTestTeardownScripts(project: Project, test: Test): List<String> {
            if (project.hasProperty("testTeardownScripts")) {
                return project.property("testTeardownScripts").toString().split(",")
            }

            getBaseTest(project)?.let { base ->
                return base.tearDownScripts
            }

            return test.tearDownScripts
        }

        fun hasTests(project: Project): Boolean {
            return !DeployExtensionUtil.getExtension(project).tests.isEmpty()
        }

        fun getBaseTest(project: Project): Test? {
            return DeployExtensionUtil.getExtension(project).tests.find { test -> test.base }
        }

        fun getExecutableTests(project: Project): List<Test> {
            return DeployExtensionUtil.getExtension(project).tests
                .filter { test ->
                    if (project.hasProperty("testName"))
                        test.name == project.property("testName").toString() else
                        true
                }
                .filter { test -> !test.base }
                .map { test ->
                    test.baseDirectory = getTestBaseDirectory(project, test)
                    test.environments = getTestEnvironments(project, test)
                    test.excludesPattern = getTestExcludePattern(project, test)
                    test.extraClassPath = getTestExtraClassPath(project, test)
                    test.scriptPattern = getTestScriptPattern(project, test)
                    test.setupScripts = getTestSetupScripts(project, test)
                    test.systemProperties = getTestSystemProperties(project, test)
                    test.tearDownScripts = getTestTeardownScripts(project, test)
                    test
                }.toList()
        }
    }
}
