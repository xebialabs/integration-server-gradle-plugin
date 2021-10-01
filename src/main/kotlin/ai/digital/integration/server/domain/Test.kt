package ai.digital.integration.server.domain

import java.io.File

open class Test(val name: String) {
    var base: Boolean = false
    var baseDirectory: File? = null
    var environments: Map<String, String> = mutableMapOf()
    var excludesPattern: String = "/(setup|teardown).py$"
    var extraClassPath: List<File> = mutableListOf()
    var scriptPattern: String = "/(.+)[.](py|cli)/"
    var setupScripts: List<String> = mutableListOf()
    var systemProperties: Map<String, String> = mutableMapOf()
    var tearDownScripts: List<String> = mutableListOf()
}
