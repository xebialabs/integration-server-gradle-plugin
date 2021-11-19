package ai.digital.integration.server.common.domain.providers.operator

import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer

interface OperatorProviderContainer : NamedDomainObjectContainer<Provider> {
    var activeProviderName: String

    fun awsEks(): AwsEksProvider
    fun awsEks(closure: Closure<*>): AwsEksProvider
    fun awsEks(action: Action<in Provider>): AwsEksProvider

    fun awsOpenshift(): AwsOpenshiftProvider
    fun awsOpenshift(closure: Closure<*>): AwsOpenshiftProvider
    fun awsOpenshift(action: Action<in Provider>): AwsOpenshiftProvider

    fun azureAks(): AzureAksProvider
    fun azureAks(closure: Closure<*>): AzureAksProvider
    fun azureAks(action: Action<in Provider>): AzureAksProvider

    fun gcpGke(): GcpGkeProvider
    fun gcpGke(closure: Closure<*>): GcpGkeProvider
    fun gcpGke(action: Action<in Provider>): GcpGkeProvider

    fun onPremise(): OnPremiseProvider
    fun onPremise(closure: Closure<*>): OnPremiseProvider
    fun onPremise(action: Action<in Provider>): OnPremiseProvider

    fun vmwareOpenshift(): VmwareOpenshiftProvider
    fun vmwareOpenshift(closure: Closure<*>): VmwareOpenshiftProvider
    fun vmwareOpenshift(action: Action<in Provider>): VmwareOpenshiftProvider
}
