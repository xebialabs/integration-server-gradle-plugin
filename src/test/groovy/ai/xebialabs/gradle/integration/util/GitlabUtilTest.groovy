package ai.xebialabs.gradle.integration.util

import ai.digital.integration.server.util.GitlabUtil
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class GitlabUtilTest {

    @Test
    void getGitlabRelativePathTest() {
        Assertions.assertEquals("gitlab/gitlab-compose/docker-compose-gitlab.yml",
                GitlabUtil.getGitlabRelativePath())
    }
}
