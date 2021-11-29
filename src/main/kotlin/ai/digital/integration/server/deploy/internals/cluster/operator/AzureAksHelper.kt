package ai.digital.integration.server.deploy.internals.cluster.operator

import org.gradle.api.Project

open class AzureAksHelper(project: Project) : OperatorHelper(project) {

    fun launchCluster() {
        cloneRepository()

    }

    fun shutdownCluster() {

    }

}
