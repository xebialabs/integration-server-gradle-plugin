package ai.digital.integration.server.tasks

import ai.digital.integration.server.util.ServerInitializeUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class PrepareDeployTask extends DefaultTask {
    static NAME = "prepareDeployTask"

    @TaskAction
    void launch() {
        ServerInitializeUtil.prepare(project)
    }
}
