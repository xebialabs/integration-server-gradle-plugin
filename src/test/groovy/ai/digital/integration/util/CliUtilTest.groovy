package ai.digital.integration.util

import ai.digital.integration.server.IntegrationServerExtension
import ai.digital.integration.server.IntegrationServerPlugin
import ai.digital.integration.server.util.CliUtil
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.util.ClosureBackedAction
import org.junit.jupiter.api.Test

class CliUtilTest {

    @Test
    void getCliBinTest() {
        /*Project project = ProjectBuilder.builder().build()
        project.plugins.apply(IntegrationServerPlugin)

        project.extensions.configure(IntegrationServerExtension, new ClosureBackedAction<IntegrationServerExtension>(
                {
                    servers {
                        controlPlane {
                            version = "10.3.0"
                            if (!project.hasProperty("useRandomItestPort")) {
                                httpPort = 4519
                            }
                        }
                    }
                    satellites {
                        satellite01 {
                        }
                    }
                    if (project.hasProperty("externalWorker")) {
                        workers {
                            worker01 {
                            }
                        }
                    }
                }
        ))
        File file = CliUtil.getCliBin(project)*/
    }
}
