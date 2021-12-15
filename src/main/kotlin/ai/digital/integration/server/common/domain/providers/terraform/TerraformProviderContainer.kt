package ai.digital.integration.server.common.domain.providers.terraform

import ai.digital.integration.server.common.domain.profiles.Profile
import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer

interface TerraformProviderContainer : NamedDomainObjectContainer<Provider> {
    var activeProviderName: String
    fun awsEks(): AwsEksProvider
    fun awsEks(closure: Closure<*>): AwsEksProvider
    fun awsEks(action: Action<in Provider>): AwsEksProvider
}
