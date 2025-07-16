package ai.digital.integration.server.util

import ai.digital.integration.server.common.util.FileUtil
import ai.digital.integration.server.common.util.YamlFileUtil
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class YamlFileUtilTest {

    @Test
    fun fileOverrideTest() {
        val initialContent = """
            deploy.server:
                downloads:
                  export-root: exports
                ssl:
                  enabled: false
        """
        val initialFile = File.createTempFile("deploy-server", "initial")
        initialFile.writeText(initialContent)
        initialFile.deleteOnExit()

        val destinationFile = File.createTempFile("deploy-server", "updated")
        destinationFile.deleteOnExit()

        YamlFileUtil.overlayFile(initialFile,
            mutableMapOf("deploy.server.downloads.export-root" to "/tmp"), destinationFile)

        assertEquals("/tmp", YamlFileUtil.readFileKey(destinationFile, "deploy.server.downloads.export-root"))
        assertEquals(false, YamlFileUtil.readFileKey(destinationFile, "deploy.server.ssl.enabled"))
    }



    @Test
    fun fileCreateTest() {
        val destinationFile = File.createTempFile("deploy-server", "updated")
        destinationFile.delete()

        try {
            YamlFileUtil.overlayFile(destinationFile,
                mutableMapOf(
                    "deploy.server.export-cis.export-dir" to "export",
                    "deploy.server.export-cis.import-work-dir" to "work")
            )

            assertEquals("export", YamlFileUtil.readFileKey(destinationFile, "deploy.server.export-cis.export-dir"))
            assertEquals("work", YamlFileUtil.readFileKey(destinationFile, "deploy.server.export-cis.import-work-dir"))
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

    @Test
    fun updateItemMultiDocYamlTest() {
        val (initialFile, destinationFile) = getInitialAndUpdatedFiles("yaml/applications.yaml")

        YamlFileUtil.overlayFile(initialFile,
            mutableMapOf("spec[0].children[0].name" to "1.0.1"),
            destinationFile)

        val expectedResultSteam =
            {}::class.java.classLoader.getResourceAsStream("yaml/applications-expected-update.yaml")

        expectedResultSteam?.let {
            val expectedTree = YamlFileUtil.readTree(it)
            val actualTree = YamlFileUtil.readTree(destinationFile.inputStream())
            assertEquals(expectedTree, actualTree)
        }
    }

    @Test
    fun updateAppWithFileTest() {
        val (initialFile, destinationFile) = getInitialAndUpdatedFiles("yaml/app-with-file.yaml")

        YamlFileUtil.overlayFile(initialFile,
            mutableMapOf("spec[0].children[0].name" to "1.0.1"),
            destinationFile)

        val expectedResultSteam =
            {}::class.java.classLoader.getResourceAsStream("yaml/app-with-file-expected-update.yaml")

        expectedResultSteam?.let {
            val expectedTree = YamlFileUtil.readTree(it)
            val actualTree = YamlFileUtil.readTree(destinationFile.inputStream())
            assertEquals(expectedTree, actualTree)
        }
    }

    private fun getInitialAndUpdatedFiles(resource: String): Pair<File, File> {
        val initialFile = File.createTempFile("deploy-server", "initial")

        val fileStream = {}::class.java.classLoader.getResourceAsStream(resource)
        fileStream?.let {
            FileUtil.copyFile(it, initialFile.toPath())
        }
        initialFile.deleteOnExit()

        val destinationFile = File.createTempFile("deploy-server", "updated")
        destinationFile.deleteOnExit()
        return Pair(initialFile, destinationFile)
    }
}
