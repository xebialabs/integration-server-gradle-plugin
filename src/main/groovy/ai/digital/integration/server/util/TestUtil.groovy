package ai.digital.integration.server.util

import ai.digital.integration.server.domain.Test
import org.gradle.api.Project

class TestUtil {

    static File getTestBaseDirectory(Project project, Test test) {
        project.hasProperty("testBaseSubDirectory") ?
                new File(test.baseDirectory, project.getProperty("testBaseSubDirectory")) :
                hasBaseTest(project) && getBaseTest(project).baseDirectory ?
                        getBaseTest(project).baseDirectory : test.baseDirectory
    }

    static Map<String, String> getTestEnvironments(Project project, Test test) {
        hasBaseTest(project) && getBaseTest(project).environments ?
                getBaseTest(project).environments : test.environments
    }

    static String getTestExcludePattern(Project project, Test test) {
        hasBaseTest(project) && getBaseTest(project).excludesPattern ?
                getBaseTest(project).excludesPattern : test.excludesPattern
    }

    static List<File> getTestExtraClassPath(Project project, Test test) {
        hasBaseTest(project) && getBaseTest(project).extraClassPath ?
                getBaseTest(project).extraClassPath : test.extraClassPath
    }

    static Map<String, String> getTestSystemProperties(Project project, Test test) {
        hasBaseTest(project) && getBaseTest(project).systemProperties ?
                getBaseTest(project).systemProperties : test.systemProperties
    }

    static def getTestScriptPattern(Project project, Test test) {
        project.hasProperty("testScriptPattern") ?
                project.getProperty("testScriptPattern") :
                hasBaseTest(project) && getBaseTest(project).scriptPattern ?
                        getBaseTest(project).scriptPattern : test.scriptPattern
    }

    static List<String> getTestSetupScripts(Project project, Test test) {
        project.hasProperty("testSetupScripts") ?
                project.getProperty("testSetupScripts").split(",") :
                hasBaseTest(project) && getBaseTest(project).setupScripts ?
                        getBaseTest(project).setupScripts : test.setupScripts
    }

    static List<String> getTestTeardownScripts(Project project, Test test) {
        project.hasProperty("testTeardownScripts") ?
                project.getProperty("testTeardownScripts").split(",") :
                hasBaseTest(project) && getBaseTest(project).tearDownScripts ?
                        getBaseTest(project).tearDownScripts : test.tearDownScripts
    }

    static boolean hasBaseTest(Project project) {
        !ExtensionUtil.getExtension(project).tests.findAll { Test test -> test.base }.isEmpty()
    }

    static Test getBaseTest(Project project) {
        ExtensionUtil.getExtension(project).tests.find { Test test -> test.base }
    }

    static List<Test> getExecutableTests(Project project) {
        ExtensionUtil.getExtension(project).tests
                .findAll { Test test ->
                    project.hasProperty("testName") ?
                            test.name.equals(project.getProperty("testName")) :
                            true
                }
                .findAll { Test test -> !test.base }
                .collect { Test test ->
                    test.setBaseDirectory(getTestBaseDirectory(project, test))
                    test.setEnvironments(getTestEnvironments(project, test))
                    test.setExcludesPattern(getTestExcludePattern(project, test))
                    test.setExtraClassPath(getTestExtraClassPath(project, test))
                    test.setScriptPattern(getTestScriptPattern(project, test))
                    test.setSetupScripts(getTestSetupScripts(project, test))
                    test.setSystemProperties(getTestSystemProperties(project, test))
                    test.setTearDownScripts(getTestTeardownScripts(project, test))
                    test
                }.toList()
    }
}
