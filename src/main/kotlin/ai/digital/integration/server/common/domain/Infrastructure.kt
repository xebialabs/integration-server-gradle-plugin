package ai.digital.integration.server.common.domain

@Suppress("UnstableApiUsage")
open class Infrastructure(val name: String) {
    var dockerComposeFiles: List<String> = mutableListOf()

    fun isDockerBased(): Boolean {
        return dockerComposeFiles.isNotEmpty()
    }
}