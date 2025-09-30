package ai.digital.integration.server.deploy.domain

import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.property
import java.io.File

@Suppress("UnstableApiUsage")
class Cli(objects: ObjectFactory) {
    var copyBuildArtifacts: Map<String, String> =
        objects.mapProperty(String::class.java, String::class.java).value(mutableMapOf()).get()
    var cleanDefaultExtContent: Boolean = objects.property<Boolean>().value(false).get()
    var debugPort: Int? = objects.property(Int::class.java).orNull
    var debugSuspend: Boolean = objects.property<Boolean>().value(false).get()
    var enabled: Boolean = objects.property<Boolean>().value(true).get()
    var filesToExecute: List<File> = objects.listProperty(File::class.java).value(mutableListOf<File>()).get()
    var overlays: Map<String, List<*>> =
        objects.mapProperty(String::class.java, List::class.java).value(mutableMapOf()).get()
    var socketTimeout: Int = objects.property<Int>().value(60000).get()
    var version: String? = objects.property(String::class.java).orNull
    var environments: Map<String, String> =
            objects.mapProperty(String::class.java, String::class.java).value(mutableMapOf()).get()
}
