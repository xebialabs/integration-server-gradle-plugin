package ai.digital.integration.server.util

import io.mockk.every
import io.mockk.mockkObject
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


class CentralConfigurationUtilTest {

    @Test
    fun getEnvTest() {
        val project = ProjectBuilder.builder().build()
        mockkObject(DeployServerUtil.Companion)
        every { DeployServerUtil.Companion.getServerWorkingDir(project) } answers { "build/resources/test" }

        val port = CentralConfigurationUtil.readServerKey(project, "deploy.server.port")
        assertEquals(port, "8180")
    }

}
