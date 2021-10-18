package ai.digital.integration.server.util

import ai.digital.integration.server.common.util.PropertiesUtil
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class PropertiesUtilTest {
    @Test
    fun readAndWritePropertiesFileTest() {
        val file = getDeployitConfFile()
        val properties = PropertiesUtil.readPropertiesFile(file)
        properties["http.context.root"] = "/"
        properties["threads.min"] = "30"

        PropertiesUtil.writePropertiesFile(file, properties)

        val newProperties = PropertiesUtil.readPropertiesFile(file)
        assertEquals("4516", newProperties["http.port"])
        assertEquals("5000", newProperties["xl.spring.cloud.config.retry.initial.interval"])
        assertEquals("0.0.0.0", newProperties["http.bind.address"])
        assertEquals("/", newProperties["http.context.root"])
        assertEquals("30", newProperties["threads.min"])
    }

    private fun getDeployitConfFile(): File {
        val initialContent = """
            http.port=4516
            xl.spring.cloud.config.retry.initial.interval=5000
            http.bind.address=0.0.0.0
        """

        val file = File.createTempFile("deployit", "conf")
        file.writeText(initialContent)
        file.deleteOnExit()
        return file
    }
}
