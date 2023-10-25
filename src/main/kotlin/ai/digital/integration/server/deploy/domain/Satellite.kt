package ai.digital.integration.server.deploy.domain

open class Satellite(val name: String) {
    var pekkoStreamingPort: Int = 8480
    var debugPort: Int? = null
    var debugSuspend: Boolean = false
    var metricsPort: Int = 8080
    var overlays: Map<String, List<Any>> = mutableMapOf()
    var serverPekkoBindHostName: String = "0.0.0.0"
    var serverPekkoHostname: String = "127.0.0.1"
    var serverPekkoPort: Int = 8380
    var stdoutFileName: String? = null
    var syncPlugins: Boolean = true
    var version: String? = null
}
