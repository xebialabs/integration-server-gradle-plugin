package ai.digital.integration.server.deploy.internals.cluster.operator

import ai.digital.integration.server.common.util.ProcessUtil
import org.gradle.api.Project

abstract class OperatorHelper(val project: Project) {

    fun cloneRepository() {
        val buildDirPath = project.buildDir.toPath().toAbsolutePath().toString()
        val dest = "$buildDirPath/xl-deploy-kubernetes-operator"
        ProcessUtil.executeCommand(project,
            "git clone git@github.com:xebialabs/xl-deploy-kubernetes-operator.git $dest")
    }
}
