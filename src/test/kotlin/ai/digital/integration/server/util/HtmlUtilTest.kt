package ai.digital.integration.server.util

import ai.digital.integration.server.common.util.HtmlUtil
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets

class HtmlUtilTest {

    @Test
    fun htmlToDocumentTest1() {
        val htmlStream = {}::class.java.classLoader.getResourceAsStream("html/request.html")
        htmlStream?.let { stream ->
            val html = String(stream.readAllBytes(), StandardCharsets.UTF_8)
            val doc = HtmlUtil.htmlToDocument(html)
            val codeValue = doc.select("form input[name=\"code\"]").`val`()
            val csrfValue = doc.select("form input[name=\"csrf\"]").`val`()

            Assertions.assertEquals("my-code", codeValue)
            Assertions.assertEquals("my-csrf", csrfValue)
        }
    }

    @Test
    fun htmlToDocumentTest2() {
        val htmlStream = {}::class.java.classLoader.getResourceAsStream("html/display.html")
        htmlStream?.let { stream ->
            val html = String(stream.readAllBytes(), StandardCharsets.UTF_8)
            val doc = HtmlUtil.htmlToDocument(html)
            val codeValue = doc.select("code").text()

            Assertions.assertEquals("api-token", codeValue)
        }
    }
}
