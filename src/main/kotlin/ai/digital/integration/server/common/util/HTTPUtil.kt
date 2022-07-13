package ai.digital.integration.server.common.util

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

        fun buildRequest(url: String): HTTPBuilder {
            val timeout = 3 * 60 * 1000 // 3 min

            val http = HTTPBuilder(url)
            http.client.params.setParameter("http.connection.timeout", timeout)
            http.client.params.setParameter("http.socket.timeout", timeout)
            http.auth.basic("admin", "admin")
            http.ignoreSSLIssues()

            return http
        }

        private fun basicAuth(username: String, password: String): String {
            return "Basic " + Base64.getEncoder().encodeToString("$username:$password".toByteArray())
        }

        fun doRequest(url: String, username: String = "admin", password: String = "admin"): HttpRequest.Builder {
            return HttpRequest.newBuilder(URI(url))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Authorization", basicAuth(username, password))
                .timeout(Duration.of(3, ChronoUnit.MINUTES))
        }

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
