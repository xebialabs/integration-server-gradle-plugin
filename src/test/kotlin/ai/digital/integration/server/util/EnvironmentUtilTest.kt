package ai.digital.integration.server.util

import ai.digital.integration.server.util.EnvironmentUtil.Companion.getEnv
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.mockkObject
import org.gradle.api.Project
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class EnvironmentUtilTest {

    @Test
    fun getEnvTest() {

        val mockProject = mockkClass(Project::class)
        mockkObject(DeployServerUtil.Companion)
        every { DeployServerUtil.Companion.isTls(mockProject) } answers { false }


        // environment with only variableName
        var envMap: MutableMap<String, String> = getEnv(mockProject, "DEPLOYIT_SERVER_OPTS", false, null, null)
        Assertions.assertEquals(1, envMap.size)
        Assertions.assertEquals("-Xmx1024m", envMap["DEPLOYIT_SERVER_OPTS"])

        // environment with variableName and debugPort
        envMap = getEnv(mockProject, "DEPLOYIT_SERVER_OPTS", false, 5005, null)
        Assertions.assertEquals(1, envMap.size)
        Assertions.assertEquals("-Xmx1024m -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005 ", envMap["DEPLOYIT_SERVER_OPTS"])

        // environment with variableName, debugSuspend and debugPort
        envMap = getEnv(mockProject, "DEPLOYIT_SERVER_OPTS", true, 5005, null)
        Assertions.assertEquals(1, envMap.size)
        Assertions.assertEquals("-Xmx1024m -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005 ", envMap["DEPLOYIT_SERVER_OPTS"])

        // environment with variableName and logFileName
        envMap = getEnv(mockProject, "DEPLOYIT_SERVER_OPTS", false, null, "deployit.log")
        Assertions.assertEquals(1, envMap.size)
        Assertions.assertEquals("-Xmx1024m -DLOGFILE=\"deployit.log\"", envMap["DEPLOYIT_SERVER_OPTS"])
    }
}
