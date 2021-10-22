package ai.digital.integration.server.common.domain

import org.gradle.api.model.ObjectFactory
import java.io.File

@Suppress("UnstableApiUsage")
open class Maintenance(objects: ObjectFactory) {
    var cleanupBeforeStartup: List<File> = objects.listProperty(File::class.java).value(mutableListOf<File>()).get()
}
