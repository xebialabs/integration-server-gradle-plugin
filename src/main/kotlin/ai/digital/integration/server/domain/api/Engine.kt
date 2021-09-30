package ai.digital.integration.server.domain.api

open class Engine(val name: String) {
    var debugPort: Int? = null
    var debugSuspend: Boolean = false
    var jvmArgs: Array<String> = arrayOf("-Xmx1024m", "-Duser.timezone=UTC")
    var logLevels: Map<String, String> = mapOf<String, String>()
    var overlays: Map<String, List<Any>> = mapOf<String, List<Any>>()
    var runtimeDirectory: String? = null
    var version: String? = null
    var stdoutFileName: String? = null
}
