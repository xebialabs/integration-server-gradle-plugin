package ai.xebialabs.gradle.integration.util

import ai.digital.integration.server.util.PropertyUtil
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals

class PropertyUtilTest {

    @Test
    void resolveValueTest() {
        Project project = ProjectBuilder.builder().build()
        project.ext.database = 'postgres'
        project.ext.mqPort = null
        project.ext.dbPort = 5432

        assertEquals("postgres",
                PropertyUtil.resolveValue(project, "database", "derby-inmemory"))
        assertEquals("rabbitmq", PropertyUtil.resolveValue(project, "mq", "rabbitmq"))

        assertEquals(null, PropertyUtil.resolveIntValue(project, "mqPort", null))
        assertEquals(5432, PropertyUtil.resolveIntValue(project, "dbPort", null))
    }
}
