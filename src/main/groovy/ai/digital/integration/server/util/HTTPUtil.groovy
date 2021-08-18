package ai.digital.integration.server.util

import groovyx.net.http.HTTPBuilder

class HTTPUtil {
    static HTTPBuilder buildRequest(String url) {
        def timeout = 3 * 60 * 1000 // 3 min

        def http = new HTTPBuilder(url)
        http.getClient().getParams().setParameter("http.connection.timeout", new Integer(timeout))
        http.getClient().getParams().setParameter("http.socket.timeout", new Integer(timeout))
        http.auth.basic("admin", "admin")
        http
    }

    static int findFreePort() {
        ServerSocket socket = null
        try {
            socket = new ServerSocket(0)
            socket.setReuseAddress(true)
            int port = socket.getLocalPort()
            try {
                socket.close()
            } catch (ignore) {
            }
            return port
        } catch (ignore) {
        } finally {
            if (socket != null) {
                try {
                    socket.close()
                } catch (ignore) {
                }
            }
        }
        throw new IllegalStateException("Could not find a free TCP/IP port to start Integration Test Server")
    }
}
