package ai.digital.integration.util

import ai.digital.integration.server.util.PropertiesUtil
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals

class PropertiesUtilTest {

    @Test
    void readAndWritePropertiesFileTest() {
        File file = getDeployitConfFile();
        def properties = PropertiesUtil.readPropertiesFile(file)
        properties.put("http.context.root", "/")
        properties.put("threads.min", "30")

        PropertiesUtil.writePropertiesFile(file, properties)

        Properties newProperties = PropertiesUtil.readPropertiesFile(file)
        assertEquals("4516", newProperties.get("http.port"))
        assertEquals("5000", newProperties.get("xl.spring.cloud.config.retry.initial.interval"))
        assertEquals("0.0.0.0", newProperties.get("http.bind.address"))
        assertEquals("/", newProperties.get("http.context.root"))
        assertEquals("30", newProperties.get("threads.min"))
    }

    private File getDeployitConfFile() {
        def initialContent = '''\
http.port=4516
xl.spring.cloud.config.retry.initial.interval=5000
http.bind.address=0.0.0.0
'''
        File file = File.createTempFile("deployit", "conf")
        file.write(initialContent)
        file.deleteOnExit()
        file
    }
}
