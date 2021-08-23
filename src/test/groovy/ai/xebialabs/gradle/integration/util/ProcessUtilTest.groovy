package ai.xebialabs.gradle.integration.util

import ai.digital.integration.server.util.ProcessUtil
import org.junit.jupiter.api.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.jupiter.api.Assertions.assertEquals

class ProcessUtilTest {

    @Test
    void execTest() {
        ProcessUtil.exec([
                command     : "process-test",
                params      : ["Digital.ai", "Deploy"],
                workDir     : getScriptDir(),
                redirectTo  : getLogFile(),
                wait        : true
        ])
        String expected = 'My first name is Digital.ai\r\n' +
                'My surname is Deploy\r\n' +
                'Total number of arguments is 2\r\n'
        Path fileName = Path.of("build/resources/test/scripts/process.log");
        String actual = Files.readString(fileName)
        assertEquals(expected, actual)
    }

    private def getScriptDir() {
        Paths.get("build/resources/test", "scripts").toFile()
    }
    private def getLogFile() {
        def file = Paths.get("build/resources/test/scripts/process.log").toFile()
        if (!file.exists()) {
            file.createNewFile()
        }
        file
    }
}
