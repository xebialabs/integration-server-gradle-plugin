package ai.digital.integration.server.common.domain.profiles

import ai.digital.integration.server.common.domain.providers.operator.OperatorProviderContainer
import ai.digital.integration.server.common.domain.providers.terraform.TerraformProviderContainer
import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.internal.NamedDomainObjectContainerConfigureDelegate
import org.gradle.util.ConfigureUtil

@Suppress("UnstableApiUsage")
internal class DefaultProfileContainer(delegate: NamedDomainObjectContainer<Profile>) :
    ProfileContainer, NamedDomainObjectContainer<Profile> by delegate {

    override fun dockerCompose(): DockerComposeProfile = dockerCompose {}

    override fun dockerCompose(closure: Closure<*>): DockerComposeProfile {
        return dockerCompose(ConfigureUtil.configureUsing(closure))
    }

    override fun dockerCompose(action: Action<in Profile>): DockerComposeProfile {
        return (findByName("dockerCompose")
            ?: create("dockerCompose") {
                action.execute(this)
            }) as DockerComposeProfile
    }

    override fun operator(): OperatorProfile = operator {}

    override fun operator(closure: Closure<*>): OperatorProfile {
        return operator(ConfigureUtil.configureUsing(closure))
    }

    override fun operator(action: Action<in Profile>): OperatorProfile {
        return (findByName("operator")
            ?: create("operator") {
                action.execute(this)
            }) as OperatorProfile
    }

    override fun terraform(): TerraformProfile = terraform {}

    override fun terraform(closure: Closure<*>): TerraformProfile {
        return terraform(ConfigureUtil.configureUsing(closure))
    }

    override fun terraform(action: Action<in Profile>): TerraformProfile {
        return (findByName("terraform")
            ?: create("terraform") {
                action.execute(this)
            }) as TerraformProfile
    }

    override fun xlBlueprint(): XlBlueprintProfile = xlBlueprint {}

    override fun xlBlueprint(closure: Closure<*>): XlBlueprintProfile {
        return xlBlueprint(ConfigureUtil.configureUsing(closure))
    }
    override fun xlBlueprint(action: Action<in Profile>): XlBlueprintProfile {
        return (findByName("xlBlueprint")
                ?: create("xlBlueprint") {
                    action.execute(this)
                }) as XlBlueprintProfile
    }

    override fun configure(configureClosure: Closure<*>): NamedDomainObjectContainer<Profile> =
        ConfigureUtil.configureSelf(configureClosure,
            this,
            NamedDomainObjectContainerConfigureDelegate(configureClosure, this))

}
