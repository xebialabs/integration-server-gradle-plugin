package ai.digital.integration.server.domain

import java.io.File

class Cli(val name: String) {
    var copyBuildArtifacts: Map<String, String> = mapOf()
    var cleanDefaultExtContent: Boolean = false
    var debugPort: Int? = null
    var debugSuspend: Boolean = false
    var filesToExecute: List<File> = listOf()
    var overlays: Map<String, List<Any>> = mapOf()
    var socketTimeout: Int = 60000
    var version: String? = null
}
