package ai.digital.integration.server.common.domain

import ai.digital.integration.server.common.domain.profiles.DockerComposeProfile
import ai.digital.integration.server.common.domain.profiles.OperatorProfile
import ai.digital.integration.server.common.domain.profiles.TerraformProfile
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.property

@Suppress("UnstableApiUsage")
open class ClusterProfiles(objects: ObjectFactory) {

    var dockerCompose: DockerComposeProfile =
        objects.property<DockerComposeProfile>().value(DockerComposeProfile(objects)).get()

    var operator: OperatorProfile =
        objects.property<OperatorProfile>().value(OperatorProfile(objects)).get()

    var terraform: TerraformProfile =
        objects.property<TerraformProfile>().value(TerraformProfile(objects)).get()

}
