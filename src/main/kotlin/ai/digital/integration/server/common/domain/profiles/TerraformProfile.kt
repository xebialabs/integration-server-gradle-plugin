package ai.digital.integration.server.common.domain.profiles

import ai.digital.integration.server.common.domain.providers.AwsEksProvider
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.property

@Suppress("UnstableApiUsage")
open class TerraformProfile(objects: ObjectFactory) {

    var awsEksProvider: AwsEksProvider =
        objects.property<AwsEksProvider>().value(AwsEksProvider(objects)).get()
}
