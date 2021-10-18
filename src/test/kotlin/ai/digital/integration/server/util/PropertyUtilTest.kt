package ai.digital.integration.server.util

import ai.digital.integration.server.common.util.PropertyUtil
import org.gradle.kotlin.dsl.extra
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

internal class PropertyUtilTest {

    @Test
    fun resolveValueTest() {
        val project = ProjectBuilder.builder().build()
        project.extra.set("database", "postgres")
        project.extra.set("mqPort", null)
        project.extra.set("dbPort", 5432)

        assertEquals(
            "postgres",
            PropertyUtil.resolveValue(project, "database", "derby-inmemory")
        )
        assertEquals("rabbitmq", PropertyUtil.resolveValue(project, "mq", "rabbitmq"))

        assertEquals(null, PropertyUtil.resolveIntValue(project, "mqPort", null))
        assertEquals(5432, PropertyUtil.resolveIntValue(project, "dbPort", null))
    }
}
