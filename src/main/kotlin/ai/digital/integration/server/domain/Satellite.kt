package ai.digital.integration.server.domain

open class Satellite(val name: String) {

    var akkaStreamingPort: Int = 8480
    var debugPort: Int? = null
    var debugSuspend: Boolean = false
    var metricsPort: Int = 8080
    var serverAkkaPort: Int = 8380
    var serverAkkaHostname: String = "127.0.0.1"
    var serverAkkaBindHostName: String = "0.0.0.0"
    var overlays: Map<String, List<Any>> = mapOf()
    var version: String? = null
    var stdoutFileName: String? = null
}
