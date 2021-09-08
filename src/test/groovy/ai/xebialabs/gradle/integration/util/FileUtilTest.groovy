package ai.xebialabs.gradle.integration.util

import ai.digital.integration.server.util.FileUtil
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Files
import java.nio.file.Paths

import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertTrue

class FileUtilTest {

    final Logger logger = LoggerFactory.getLogger(FileUtilTest.class)

    @Test
    void copyFileTest() {
        File sourceFile = new File(FileUtilTest.class.classLoader.getResource("centralConfiguration/deploy-server.yaml").getFile())
        String destPath = new File("build/resources/test/copyTest").getAbsolutePath()

        logger.info("Source file path is: ${sourceFile.getAbsolutePath()}")
        logger.info("Destination file path is: $destPath")
        new File(destPath).mkdirs()

        FileUtil.copyFile(new FileInputStream(sourceFile), Paths.get(destPath + "/deploy-server-copied.yaml"))
        File destFile = new File(destPath + "/deploy-server-copied.yaml")
        assertTrue(destFile.exists())
        assertTrue(FileUtils.contentEquals(sourceFile, destFile));
        new File(destPath).deleteDir()
    }

    @Test
    void grantRWPermissionsTest() {
        String fileContent = "New file to test"
        File file = File.createTempFile("permission-test", "initial")
        file.write(fileContent)
        file.setReadOnly()
        file.deleteOnExit()

        assertTrue(Files.isReadable(file.toPath()))
        assertFalse(Files.isWritable(file.toPath()))

        FileUtil.grantRWPermissions(file)

        assertTrue(Files.isReadable(file.toPath()))
        assertTrue(Files.isWritable(file.toPath()))
    }
}
