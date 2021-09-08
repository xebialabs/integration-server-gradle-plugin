package ai.xebialabs.gradle.integration.util

import ai.digital.integration.server.util.YamlFileUtil
import org.junit.jupiter.api.Test

import java.nio.charset.StandardCharsets

import static org.junit.jupiter.api.Assertions.assertEquals


class YamlFileUtilTest {

    @Test
    void resourceOverrideTest() {
        File deployServerYaml = new File(URLDecoder.decode(YamlFileUtilTest.class.classLoader.getResource(
                "centralConfiguration/deploy-server.yaml").getFile(),
                StandardCharsets.UTF_8))

        File destinationFile = File.createTempFile("deploy-server", "updated")
        destinationFile.deleteOnExit()

        YamlFileUtil.overlayResource(deployServerYaml.toURI().toURL(),
                ["deploy.server.port": 15000], destinationFile)

        assertEquals(15000, YamlFileUtil.readFileKey(destinationFile, "deploy.server.port"))
    }

    @Test
    void fileOverrideTest() {
        def initialContent = '''\
deploy.server:
  hostname: "127.0.0.1"
  port: 8080
'''
        File initialFile = File.createTempFile("deploy-server", "initial")
        initialFile.write(initialContent)
        initialFile.deleteOnExit()

        File destinationFile = File.createTempFile("deploy-server", "updated")
        destinationFile.deleteOnExit()

        YamlFileUtil.overlayFile(initialFile,
                ["deploy.server.hostname": "localhost"], destinationFile)

        assertEquals("localhost", YamlFileUtil.readFileKey(destinationFile, "deploy.server.hostname"))
        assertEquals(8080, YamlFileUtil.readFileKey(destinationFile, "deploy.server.port"))
    }

    @Test
    void fileOverride2Test() {
        def initialContent = '''\
deploy:
  server:
    hostname: "127.0.0.1"
    port: 8080
'''
        File initialFile = File.createTempFile("deploy-server", "initial")
        initialFile.write(initialContent)
        initialFile.deleteOnExit()

        File destinationFile = File.createTempFile("deploy-server", "updated")
        destinationFile.deleteOnExit()

        YamlFileUtil.overlayFile(initialFile,
                ["deploy.server.hostname": "localhost"], destinationFile)

        assertEquals("localhost", YamlFileUtil.readFileKey(destinationFile, "deploy.server.hostname"))
        assertEquals(8080, YamlFileUtil.readFileKey(destinationFile, "deploy.server.port"))
    }


    @Test
    void fileCreateTest() {
        File destinationFile = File.createTempFile("deploy-server", "updated")
        destinationFile.delete()

        try {
            YamlFileUtil.overlayFile(destinationFile,
                    ["deploy.server.hostname": "www.digital.ai", "deploy.server.port": 9595])

            assertEquals("www.digital.ai", YamlFileUtil.readFileKey(destinationFile, "deploy.server.hostname"))
            assertEquals(9595, YamlFileUtil.readFileKey(destinationFile, "deploy.server.port"))
        } finally {
            destinationFile.deleteOnExit()
        }
    }

}
