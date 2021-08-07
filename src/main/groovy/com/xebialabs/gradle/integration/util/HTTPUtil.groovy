package com.xebialabs.gradle.integration.util

import groovyx.net.http.HTTPBuilder

class HTTPUtil {
    static HTTPBuilder buildRequest(String url) {
        def http = new HTTPBuilder(url)
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
