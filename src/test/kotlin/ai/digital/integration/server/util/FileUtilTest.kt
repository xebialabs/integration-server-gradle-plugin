package ai.digital.integration.server.util

import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.nio.file.Files

class FileUtilTest {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    @Test
    fun copyFileTest() {
        val sourceFile =
            File(URLDecoder.decode({}::class.java.classLoader.getResource("centralConfiguration/deploy-server.yaml")?.file,
                StandardCharsets.UTF_8))
        val destPath = File("build/resources/test/copyTest")

        try {
            logger.info("Source file path is: ${sourceFile.absolutePath}")
            logger.info("Destination file path is: ${destPath.absolutePath}")

            val destFile = File(destPath, "deploy-server-copied.yaml")
            FileUtil.copyFile(FileInputStream(sourceFile), destFile.toPath())
            assertTrue(destFile.exists())
            assertTrue(FileUtils.contentEquals(sourceFile, destFile))
        } finally {
            destPath.deleteRecursively()
        }
    }

    @Test
    fun grantRWPermissionsTest() {
        val fileContent = "New file to test"
        val file = File.createTempFile("permission-test", "initial")
        file.writeText(fileContent)
        file.setReadOnly()
        file.deleteOnExit()

        assertTrue(Files.isReadable(file.toPath()))
        assertFalse(Files.isWritable(file.toPath()))

        FileUtil.grantRWPermissions(file)

        assertTrue(Files.isReadable(file.toPath()))
        assertTrue(Files.isWritable(file.toPath()))
    }
}
