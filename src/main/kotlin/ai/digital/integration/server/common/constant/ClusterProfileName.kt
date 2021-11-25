package ai.digital.integration.server.common.constant

enum class ClusterProfileName(val profileName: String) {
    DOCKER_COMPOSE("docker-compose"),
    OPERATOR("operator"),
    TERRAFORM("terraform")
}
