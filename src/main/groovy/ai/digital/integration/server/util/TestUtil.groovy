package ai.digital.integration.server.util

import ai.digital.integration.server.domain.Test
import org.gradle.api.Project

class TestUtil {

    static File getTestBaseDirectory(Project project, Test test) {
        project.hasProperty("testBaseSubDirectory") ?
                new File(test.baseDirectory, project.getProperty("testBaseSubDirectory")) :
                test.baseDirectory
    }

    static def getTestScriptPattern(Project project, Test test) {
        project.hasProperty("testScriptPattern") ? project.getProperty("testScriptPattern") : test.scriptPattern
    }

    static List<String> getTestSetupScripts(Project project, Test test) {
        project.hasProperty("testSetupScripts") ? project.getProperty("testSetupScripts").split(",") : test.setupScripts
    }

    static List<String> getTestTeardownScript(Project project, Test test) {
        project.hasProperty("testTeardownScripts") ? project.getProperty("testTeardownScripts").split(",") : test.tearDownScripts
    }
}
