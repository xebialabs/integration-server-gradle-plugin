package ai.digital.integration.server.util

import ai.digital.integration.server.common.util.GitlabUtil
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class GitlabUtilTest {

    @Test
    fun getGitlabRelativePathTest() {
        Assertions.assertEquals("gitlab/gitlab-compose/docker-compose-gitlab.yml",
            GitlabUtil.getGitlabRelativePath())
    }
}
