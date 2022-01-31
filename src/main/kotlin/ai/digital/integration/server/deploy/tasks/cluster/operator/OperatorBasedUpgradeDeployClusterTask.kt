package ai.digital.integration.server.deploy.tasks.cluster.operator

import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.tasks.cluster.operator.OperatorBasedUpgradeClusterTask

open class OperatorBasedUpgradeDeployClusterTask : OperatorBasedUpgradeClusterTask(ProductName.DEPLOY) {

    companion object {
        const val NAME = "operatorBasedUpgradeDeployCluster"
    }
}
