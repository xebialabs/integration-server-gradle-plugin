package ai.digital.integration.server.common.domain.providers.terraform

import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.property

@Suppress("UnstableApiUsage")
open class AwsEksProvider(objects: ObjectFactory) {
    var clusterVersion: String = objects.property<String>().value("1.17").get()
    var name: String? = objects.property<String>().orNull
    var vpcId: String? = objects.property<String>().orNull
    var vpcVersion: String? = objects.property<String>().orNull
    var version: String = objects.property<String>().value("17.18.0").get()
}
