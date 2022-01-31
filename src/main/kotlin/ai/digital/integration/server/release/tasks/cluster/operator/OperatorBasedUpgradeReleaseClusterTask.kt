package ai.digital.integration.server.release.tasks.cluster.operator

import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.tasks.cluster.operator.OperatorBasedUpgradeClusterTask

open class OperatorBasedUpgradeReleaseClusterTask : OperatorBasedUpgradeClusterTask(ProductName.RELEASE) {

    companion object {
        const val NAME = "operatorBasedUpgradeReleaseCluster"
    }
}
