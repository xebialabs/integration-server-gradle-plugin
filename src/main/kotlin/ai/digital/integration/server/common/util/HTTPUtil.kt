package ai.digital.integration.server.common.util

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.*

open class HTTPUtil {
    companion object {

        fun buildRequest(url: String, username: String = "admin", password: String = "admin"): HttpRequest.Builder {
            val timeout = Duration.of(3, ChronoUnit.MINUTES)
            return HttpRequest.newBuilder(URI(url))
                .timeout(timeout)
                .header("Authorization", basicAuth(username, password))
        }

        private fun basicAuth(username: String, password: String): String {
            return "Basic " + Base64.getEncoder().encodeToString("$username:$password".toByteArray())
        }

        fun doRequest(url: String, username: String = "admin", password: String = "admin"): HttpRequest.Builder {
            return buildRequest(url, username, password)
        }

        fun findFreePort(): Int {
            var socket: java.net.ServerSocket? = null
            try {
                socket = java.net.ServerSocket(0)
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
