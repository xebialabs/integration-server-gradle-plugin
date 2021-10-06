package ai.digital.integration.util

import ai.digital.integration.server.util.CentralConfigurationUtil
import ai.digital.integration.server.util.DeployServerUtil
import groovy.mock.interceptor.MockFor
import org.gradle.api.Project
import org.junit.jupiter.api.Test

class CentralConfigurationUtilTest {

    @Test
    void readServerKeyTest() {
        def mock = new MockFor(DeployServerUtil)
        mock.demand.getServerWorkingDir { project -> "build/resources/test" }
        mock.use {
            Project project
            String port = CentralConfigurationUtil.readServerKey(project, "deploy.server.port").toString()
            assert port == "8180"
        }
    }
}
