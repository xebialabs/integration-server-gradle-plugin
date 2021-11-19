package ai.digital.integration.server.common.domain.profiles

import ai.digital.integration.server.common.constant.OperatorProviderName
import ai.digital.integration.server.common.domain.providers.operator.*
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.container
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

@Suppress("UnstableApiUsage")
open class OperatorProfile @Inject constructor(@Input var name: String, project: Project) : Profile {

    @Input
    val activeProviderName = project.objects.property<String>().value(OperatorProviderName.ON_PREMISE.providerName)

    val awsOpenshift: AwsOpenshiftProvider =
        DefaultOperatorProviderContainer(project.container(Provider::class) { name ->
            project.objects.newInstance(AwsOpenshiftProvider::class, project)
        }, project).awsOpenshift()

    fun awsOpenshift(action: Action<in AwsOpenshiftProvider>) {
        action.execute(awsOpenshift)
    }

    val awsEks: AwsEksProvider =
        DefaultOperatorProviderContainer(project.container(Provider::class) { name ->
            project.objects.newInstance(AwsEksProvider::class, project)
        }, project).awsEks()

    fun awsEks(action: Action<in AwsEksProvider>) = action.execute(awsEks)

    val azureAks: OperatorProviderContainer =
        DefaultOperatorProviderContainer(project.container(Provider::class) { name ->
            project.objects.newInstance(AzureAksProvider::class, name, project)
        }, project)

    fun azureAks(action: Action<in OperatorProviderContainer>) = action.execute(azureAks)

    val gcpGke: OperatorProviderContainer =
        DefaultOperatorProviderContainer(project.container(Provider::class) { name ->
            project.objects.newInstance(AzureAksProvider::class, name, project)
        }, project)

    fun gcpGke(action: Action<in OperatorProviderContainer>) = action.execute(gcpGke)

    val onPremise: OperatorProviderContainer =
        DefaultOperatorProviderContainer(project.container(Provider::class) { name ->
            project.objects.newInstance(AzureAksProvider::class, name, project)
        }, project)

    fun onPremise(action: Action<in OperatorProviderContainer>) = action.execute(onPremise)

    val vmwareOpenshift: OperatorProviderContainer =
        DefaultOperatorProviderContainer(project.container(Provider::class) { name ->
            project.objects.newInstance(AzureAksProvider::class, name, project)
        }, project)

    fun vmwareOpenshift(action: Action<in OperatorProviderContainer>) = action.execute(vmwareOpenshift)

}
