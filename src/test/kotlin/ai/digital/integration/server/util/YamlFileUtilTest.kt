package ai.digital.integration.server.util

import ai.digital.integration.server.common.util.YamlFileUtil
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class YamlFileUtilTest {

    @Test
    fun resourceOverrideTest() {
        val deployServerStream = {}::class.java.classLoader.getResource("centralConfiguration/deploy-server.yaml")
        deployServerStream?.let { stream ->
            val deployServerYaml = File(URLDecoder.decode(stream.file, StandardCharsets.UTF_8))

            val destinationFile = File.createTempFile("deploy-server", "updated")
            destinationFile.deleteOnExit()

            YamlFileUtil.overlayResource(deployServerYaml.toURI().toURL(),
                mutableMapOf("deploy.server.port" to 15000),
                destinationFile)

            assertEquals(15000, YamlFileUtil.readFileKey(destinationFile, "deploy.server.port"))
        }

    }

    @Test
    fun fileOverrideTest() {
        val initialContent = """
            deploy.server:
                hostname: "127.0.0.1"
                port: 8080
        """
        val initialFile = File.createTempFile("deploy-server", "initial")
        initialFile.writeText(initialContent)
        initialFile.deleteOnExit()

        val destinationFile = File.createTempFile("deploy-server", "updated")
        destinationFile.deleteOnExit()

        YamlFileUtil.overlayFile(initialFile,
            mutableMapOf("deploy.server.hostname" to "localhost"), destinationFile)

        assertEquals("localhost", YamlFileUtil.readFileKey(destinationFile, "deploy.server.hostname"))
        assertEquals(8080, YamlFileUtil.readFileKey(destinationFile, "deploy.server.port"))
    }

    @Test
    fun fileOverride2Test() {
        val initialContent = """
            deploy.server:
                hostname: "127.0.0.1"
                port: 8080
        """
        val initialFile = File.createTempFile("deploy-server", "initial")
        initialFile.writeText(initialContent)
        initialFile.deleteOnExit()

        val destinationFile = File.createTempFile("deploy-server", "updated")
        destinationFile.deleteOnExit()

        YamlFileUtil.overlayFile(initialFile,
            mutableMapOf("deploy.server.hostname" to "localhost"), destinationFile)

        assertEquals("localhost", YamlFileUtil.readFileKey(destinationFile, "deploy.server.hostname"))
        assertEquals(8080, YamlFileUtil.readFileKey(destinationFile, "deploy.server.port"))
    }


    @Test
    fun fileCreateTest() {
        val destinationFile = File.createTempFile("deploy-server", "updated")
        destinationFile.delete()

        try {
            YamlFileUtil.overlayFile(destinationFile,
                mutableMapOf(
                    "deploy.server.hostname" to "www.digital.ai",
                    "deploy.server.port" to 9595)
            )

            assertEquals("www.digital.ai", YamlFileUtil.readFileKey(destinationFile, "deploy.server.hostname"))
            assertEquals(9595, YamlFileUtil.readFileKey(destinationFile, "deploy.server.port"))
        } finally {
            destinationFile.deleteOnExit()
        }
    }

    @Test
    fun updateItemInTheArrayTest() {
        val initialContent = """
            apiVersion: apps/v1
            kind: Deployment
            metadata:
              labels:
                control-plane: controller-manager
              name: xld-operator-controller-manager
            spec:
              template:
                spec:
                  containers:
                  - name: kube-rbac-proxy
                    image: gcr.io/kubebuilder/kube-rbac-proxy:v0.8.0
                  - name: manager
                    image: xebialabs/deploy-operator:1.2.0-openshift
        """
        val initialFile = File.createTempFile("deploy-server", "initial")
        initialFile.writeText(initialContent)
        initialFile.deleteOnExit()

        val destinationFile = File.createTempFile("deploy-server", "updated")
        destinationFile.deleteOnExit()

        YamlFileUtil.overlayFile(initialFile,
            mutableMapOf("spec.template.spec.containers[1].image" to "xebialabs/deploy-operator:1.0.0"),
            destinationFile)

        assertEquals("xebialabs/deploy-operator:1.0.0",
            YamlFileUtil.readFileKey(destinationFile, "spec.template.spec.containers[1].image"))
    }
}
