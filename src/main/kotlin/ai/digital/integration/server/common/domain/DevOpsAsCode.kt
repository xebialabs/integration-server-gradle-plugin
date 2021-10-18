package ai.digital.integration.server.common.domain

import java.io.File

open class DevOpsAsCode(val name: String) {
    var devOpAsCodeScript: File? = null
    var scmAuthor: String? = null
    var scmCommit: String? = null
    var scmDate: String? = null
    var scmFile: String? = null
    var scmMessage: String? = null
    var scmRemote: String? = null
    var scmType: String? = null
}
