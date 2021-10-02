package ai.digital.integration.server.util

class GitlabUtil {
    companion object {
        @JvmStatic
        fun getGitlabRelativePath(): String {
            return "gitlab/gitlab-compose/docker-compose-gitlab.yml"
        }
    }
}
