package ai.digital.integration.server.deploy.domain

open class Satellite(val name: String) {
    var akkaStreamingPort: Int = 8480
    var debugPort: Int? = null
    var debugSuspend: Boolean = false
    var metricsPort: Int = 8080
    var overlays: Map<String, List<Any>> = mutableMapOf()
    var serverAkkaBindHostName: String = "0.0.0.0"
    var serverAkkaHostname: String = "127.0.0.1"
    var serverAkkaPort: Int = 8380
    var stdoutFileName: String? = null
    var syncPlugins: Boolean = true
    var version: String? = null
}
