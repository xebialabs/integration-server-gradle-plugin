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

    static def getTestSetupScript(Project project, Test test) {
        project.hasProperty("testSetupScript") ? project.getProperty("testSetupScript") : test.setupScript
    }

    static def getTestTeardownScript(Project project, Test test) {
        project.hasProperty("testTeardownScript") ? project.getProperty("testTeardownScript") : test.tearDownScript
    }
}
