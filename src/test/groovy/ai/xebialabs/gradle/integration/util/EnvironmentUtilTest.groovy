package ai.xebialabs.gradle.integration.util

import ai.digital.integration.server.util.EnvironmentUtil
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals

class EnvironmentUtilTest {

    @Test
    void getEnvTest() {
        // environment with only variableName
        Map<String, String> envMap = EnvironmentUtil.getEnv(
                "DEPLOYIT_SERVER_OPTS", false, null, null)
        assertEquals(1, envMap.size())
        assertEquals("-Xmx1024m",
                envMap.get("DEPLOYIT_SERVER_OPTS"))

        // environment with variableName and debugPort
        envMap = EnvironmentUtil.getEnv(
                "DEPLOYIT_SERVER_OPTS", false, 5005, null)
        assertEquals(1, envMap.size())
        assertEquals("-Xmx1024m -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 ",
                envMap.get("DEPLOYIT_SERVER_OPTS"))

        // environment with variableName, debugSuspend and debugPort
        envMap = EnvironmentUtil.getEnv(
                "DEPLOYIT_SERVER_OPTS", true, 5005, null)
        assertEquals(1, envMap.size())
        assertEquals("-Xmx1024m -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005 ",
                envMap.get("DEPLOYIT_SERVER_OPTS"))

        // environment with variableName and logFileName
        envMap = EnvironmentUtil.getEnv(
                "DEPLOYIT_SERVER_OPTS", false, null, "deployit.log")
        assertEquals(1, envMap.size())
        assertEquals("-Xmx1024m -DLOGFILE=deployit.log",
                envMap.get("DEPLOYIT_SERVER_OPTS"))
    }
}
