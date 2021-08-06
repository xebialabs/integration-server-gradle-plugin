package com.xebialabs.gradle.integration.util

import groovyx.net.http.HTTPBuilder

class HTTPUtil {
    static HTTPBuilder buildRequest(String url) {
        def http = new HTTPBuilder(url)
        http.auth.basic("admin", "admin")
        http
    }
}
