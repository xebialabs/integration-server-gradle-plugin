package ai.digital.integration.server.util

import groovyx.net.http.HTTPBuilder
import java.net.ServerSocket
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.*

open class HTTPUtil {
    companion object {

        @JvmStatic
        fun buildRequest(url: String): HTTPBuilder {
            val timeout = 3 * 60 * 1000 // 3 min

            val http = HTTPBuilder(url)
            http.client.params.setParameter("http.connection.timeout", timeout)
            http.client.params.setParameter("http.socket.timeout", timeout)
            http.auth.basic("admin", "admin")
            http.ignoreSSLIssues()

            return http
        }

        private fun basicAuth(): String {
            return "Basic " + Base64.getEncoder().encodeToString("admin:admin".toByteArray())
        }

        @JvmStatic
        fun doRequest(url: String): HttpRequest.Builder {
            return HttpRequest.newBuilder(URI(url))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Authorization", basicAuth())
                .timeout(Duration.of(3, ChronoUnit.MINUTES))
        }

        @JvmStatic
        fun findFreePort(): Int {
            var socket: ServerSocket? = null
            try {
                socket = ServerSocket(0)
                socket.reuseAddress = true
                val port = socket.localPort
                try {
                    socket.close()
                } catch (ignore: Exception) {
                }
                return port
            } catch (ignore: Exception) {
            } finally {
                try {
                    socket?.close()
                } catch (ignore: Exception) {
                }
            }
            throw IllegalStateException("Could not find a free TCP/IP port to start Integration Test Server")
        }
    }
}
