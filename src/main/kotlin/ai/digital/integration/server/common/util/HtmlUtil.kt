package ai.digital.integration.server.common.util

import org.jsoup.Jsoup

class HtmlUtil {
    companion object {
        fun htmlToDocument(html: String) = Jsoup.parse(html, "UTF-8")
    }
}
