package ai.digital.integration.server.util

import groovyx.net.http.HTTPBuilder
import java.net.ServerSocket

open class HTTPUtil {
    companion object {
        @JvmStatic
        fun buildRequest(url: String): HTTPBuilder {
            val timeout = 3 * 60 * 1000 // 3 min

            val http = HTTPBuilder(url)
            http.getClient().getParams().setParameter("http.connection.timeout", timeout)
            http.getClient().getParams().setParameter("http.socket.timeout", timeout)
            http.auth.basic("admin", "admin")
            http.ignoreSSLIssues()

            return http
        }

        @JvmStatic
        fun findFreePort(): Int {
            var socket: ServerSocket? = null
            try {
                socket = ServerSocket(0)
                socket.setReuseAddress(true)
                val port = socket.getLocalPort()
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
