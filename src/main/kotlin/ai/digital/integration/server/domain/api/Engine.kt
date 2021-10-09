package ai.digital.integration.server.domain.api

open class Engine(val name: String) {
    var debugPort: Int? = null
    var debugSuspend: Boolean = false
    var jvmArgs: Array<String> = arrayOf("-Xmx1024m", "-Duser.timezone=UTC")
    var logLevels: MutableMap<String, String> = mutableMapOf()
    var overlays: MutableMap<String, MutableList<Any>> = mutableMapOf()
    var runtimeDirectory: String? = null
    var version: String? = null
    var stdoutFileName: String? = null
}
