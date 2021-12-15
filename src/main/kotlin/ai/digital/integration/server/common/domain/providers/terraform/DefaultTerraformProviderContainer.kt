package ai.digital.integration.server.common.domain.providers.terraform

import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import org.gradle.util.ConfigureUtil

@Suppress("UnstableApiUsage")
internal class DefaultTerraformProviderContainer(delegate: NamedDomainObjectContainer<Provider>, project: Project) :
    TerraformProviderContainer, NamedDomainObjectContainer<Provider> by delegate {

    private var _activeProviderName: Property<String> = project.objects.property()

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

}
