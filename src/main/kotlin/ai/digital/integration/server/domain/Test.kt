package ai.digital.integration.server.domain

import java.io.File

open class Test(val name: String) {
    var base: Boolean = false
    var baseDirectory: File? = null
    var environments: Map<String, String> = mapOf()
    var excludesPattern: String = "/(setup|teardown).py$"
    var extraClassPath: List<File> = listOf()
    var scriptPattern: String = "/(.+)[.](py|cli)/"
    var setupScripts: List<String> = listOf<String>()
    var systemProperties: Map<String, String> = mapOf()
    var tearDownScripts: List<String> = listOf()
}
