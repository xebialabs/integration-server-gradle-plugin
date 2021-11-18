package ai.digital.integration.server.common.domain.providers.operator

import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.property


@Suppress("UnstableApiUsage")
open class AwsOpenshiftProvider(objects: ObjectFactory) {
    var name: String? = objects.property<String>().orNull
}
