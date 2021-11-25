package ai.digital.integration.server.common.domain.providers.operator

import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import org.gradle.util.ConfigureUtil

@Suppress("UnstableApiUsage")
internal class DefaultOperatorProviderContainer(delegate: NamedDomainObjectContainer<Provider>, project: Project) :
    OperatorProviderContainer, NamedDomainObjectContainer<Provider> by delegate {

    private var _activeProviderName: Property<String> = project.objects.property<String>()

    override var activeProviderName: String
        get() = _activeProviderName.get()
        set(value) {
            _activeProviderName.set(value)
        }

    override fun awsEks(): AwsEksProvider = awsEks {}

    override fun awsEks(closure: Closure<*>): AwsEksProvider {
        return awsEks(ConfigureUtil.configureUsing(closure))
    }

    override fun awsEks(action: Action<in Provider>): AwsEksProvider {
        return (findByName("awsEks")
            ?: create("awsEks") {
                action.execute(this)
            }) as AwsEksProvider
    }

    override fun awsOpenshift(): AwsOpenshiftProvider = awsOpenshift {}

    override fun awsOpenshift(closure: Closure<*>): AwsOpenshiftProvider {
        return awsOpenshift(ConfigureUtil.configureUsing(closure))
    }

    override fun awsOpenshift(action: Action<in Provider>): AwsOpenshiftProvider {
        return (findByName("awsOpenshift")
            ?: create("awsOpenshift") {
                action.execute(this)
            }) as AwsOpenshiftProvider
    }

    override fun azureAks(): AzureAksProvider = azureAks {}

    override fun azureAks(closure: Closure<*>): AzureAksProvider {
        return azureAks(ConfigureUtil.configureUsing(closure))
    }

    override fun azureAks(action: Action<in Provider>): AzureAksProvider {
        return (findByName("azureAks")
            ?: create("azureAks") {
                action.execute(this)
            }) as AzureAksProvider
    }

    override fun gcpGke(): GcpGkeProvider = gcpGke {}

    override fun gcpGke(closure: Closure<*>): GcpGkeProvider {
        return gcpGke(ConfigureUtil.configureUsing(closure))
    }

    override fun gcpGke(action: Action<in Provider>): GcpGkeProvider {
        return (findByName("gcpGke")
            ?: create("gcpGke") {
                action.execute(this)
            }) as GcpGkeProvider
    }

    override fun onPremise(): OnPremiseProvider = onPremise {}

    override fun onPremise(closure: Closure<*>): OnPremiseProvider {
        return onPremise(ConfigureUtil.configureUsing(closure))
    }

    override fun onPremise(action: Action<in Provider>): OnPremiseProvider {
        return (findByName("onPremise")
            ?: create("onPremise") {
                action.execute(this)
            }) as OnPremiseProvider
    }

    override fun vmwareOpenshift(): VmwareOpenshiftProvider = vmwareOpenshift {}

    override fun vmwareOpenshift(closure: Closure<*>): VmwareOpenshiftProvider {
        return vmwareOpenshift(ConfigureUtil.configureUsing(closure))
    }

    override fun vmwareOpenshift(action: Action<in Provider>): VmwareOpenshiftProvider {
        return (findByName("vmwareOpenshift")
            ?: create("vmwareOpenshift") {
                action.execute(this)
            }) as VmwareOpenshiftProvider
    }

}
