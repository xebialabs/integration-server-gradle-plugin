package ai.digital.integration.server.deploy.domain

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import java.io.File

@Suppress("UnstableApiUsage")
class Cli(objects: ObjectFactory) {
    var copyBuildArtifacts: MapProperty<String, String> =
        objects.mapProperty(String::class.java, String::class.java).value(mutableMapOf())
    var cleanDefaultExtContent: Property<Boolean> = objects.property<Boolean>().value(false)
    var debugPort: Property<Int?> = objects.property()
    var debugSuspend: Property<Boolean> = objects.property<Boolean>().value(false)
    var filesToExecute: ListProperty<File> = objects.listProperty(File::class.java).value(mutableListOf<File>())
    var overlays: MapProperty<String, List<*>> =
        objects.mapProperty(String::class.java, List::class.java).value(mutableMapOf())
    var socketTimeout: Property<Int> = objects.property<Int>().value(60000)
    var version: Property<String?> = objects.property()
}
