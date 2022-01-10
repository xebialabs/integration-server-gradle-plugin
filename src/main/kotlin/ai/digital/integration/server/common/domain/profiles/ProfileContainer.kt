package ai.digital.integration.server.common.domain.profiles

import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer

interface ProfileContainer : NamedDomainObjectContainer<Profile> {
    fun dockerCompose(): DockerComposeProfile
    fun dockerCompose(closure: Closure<*>): DockerComposeProfile
    fun dockerCompose(action: Action<in Profile>): DockerComposeProfile

    fun operator(): OperatorProfile
    fun operator(closure: Closure<*>): OperatorProfile
    fun operator(action: Action<in Profile>): OperatorProfile

    fun terraform(): TerraformProfile
    fun terraform(closure: Closure<*>): TerraformProfile
    fun terraform(action: Action<in Profile>): TerraformProfile

    fun xlBlueprint(): XlBlueprintProfile
    fun xlBlueprint(closure: Closure<*>): XlBlueprintProfile
    fun xlBlueprint(action: Action<in Profile>): XlBlueprintProfile
}

