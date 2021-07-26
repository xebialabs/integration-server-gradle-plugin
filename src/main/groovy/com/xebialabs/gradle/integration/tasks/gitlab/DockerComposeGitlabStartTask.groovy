package com.xebialabs.gradle.integration.tasks.gitlab

import com.palantir.gradle.docker.DockerComposeUp
import com.xebialabs.gradle.integration.util.ExtensionsUtil
import com.xebialabs.gradle.integration.util.HTTPUtil
import org.apache.commons.io.IOUtils
import org.gradle.api.GradleException
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

import java.nio.file.Paths
import java.util.concurrent.TimeUnit

import static com.xebialabs.gradle.integration.util.PluginUtil.DIST_DESTINATION_NAME
import static com.xebialabs.gradle.integration.util.PluginUtil.PLUGIN_GROUP

class DockerComposeGitlabStartTask extends DockerComposeUp {
    static NAME = "dockerComposeGitlabStart"

    DockerComposeGitlabStartTask() {
        this.group = PLUGIN_GROUP
    }

    @Override
    String getDescription() {
        return "Starts gitlab instance using `docker-compose` and docker-compose-gitlab.yaml file."
    }

    @InputFiles
    File getDockerComposeFile() {
        def composeFile = "docker-compose-gitlab.yml"
        def dockerComposeStream = DockerComposeGitlabStartTask.class.classLoader
                .getResourceAsStream("gitlab/gitlab-compose/${composeFile}")
        def resultComposeFilePath = Paths.get(
                "${project.buildDir.toPath().resolve(DIST_DESTINATION_NAME).toAbsolutePath().toString()}/${composeFile}")
        def parentDir = resultComposeFilePath.getParent().toFile()
        parentDir.mkdirs()
        copyFile(dockerComposeStream, resultComposeFilePath)

        return project.file(resultComposeFilePath)
    }

    private static def copyFile(inputStream, path) {
        def file = path.toFile()
        def os = new FileOutputStream(file)
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs()
        }
        file.createNewFile()
        try {
            IOUtils.copy(inputStream, os)
        } finally {
            os.close()
        }
    }

    private void waitForBoot() {
        project.logger.lifecycle("Waiting for Gitlab to start")
        def extension = ExtensionsUtil.getExtension(project)
        int triesLeft = 120
        boolean success = false
        while (triesLeft > 0 && !success) {
            try {
                def http = HTTPUtil.buildRequest("http://localhost:11180/")
                http.get([:]) { resp, reader ->
                    println("Git Lab successfully started on port 11180")
                    success = true
                }
            } catch (ignored) {
            }
            if (!success) {
                println("Waiting for ${extension.serverPingRetrySleepTime} second(s) before retry. ($triesLeft)")
                TimeUnit.SECONDS.sleep(extension.serverPingRetrySleepTime)
                triesLeft -= 1
            }
        }
        if (!success) {
            throw new GradleException("GitlabServer failed to start")
        }
    }

    @TaskAction
    void run() {
        project.exec {
            it.executable "docker-compose"
            it.args '-f', getDockerComposeFile(), '-p', 'gitlabServer', 'up', '-d'
        }
        waitForBoot()
    }

}
