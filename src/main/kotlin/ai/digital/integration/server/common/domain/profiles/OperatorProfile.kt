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

    @Input
    val deploymentTimeoutSeconds = project.objects.property<Int>().value(600)

    @Input
    val xlCliVersion = project.objects.property<String>().value("10.2.3")

    val awsOpenshift: AwsOpenshiftProvider =
        DefaultOperatorProviderContainer(project.container(Provider::class) {
            project.objects.newInstance(AwsOpenshiftProvider::class, project)
        }, project).awsOpenshift()

    fun awsOpenshift(action: Action<in AwsOpenshiftProvider>) {
        action.execute(awsOpenshift)
    }

    val awsEks: AwsEksProvider =
        DefaultOperatorProviderContainer(project.container(Provider::class) {
            project.objects.newInstance(AwsEksProvider::class, project)
        }, project).awsEks()

    fun awsEks(action: Action<in AwsEksProvider>) = action.execute(awsEks)

    val azureAks: AzureAksProvider =
        DefaultOperatorProviderContainer(project.container(Provider::class) {
            project.objects.newInstance(AzureAksProvider::class, project)
        }, project).azureAks()

    fun azureAks(action: Action<in AzureAksProvider>) = action.execute(azureAks)

    val gcpGke: GcpGkeProvider =
        DefaultOperatorProviderContainer(project.container(Provider::class) {
            project.objects.newInstance(GcpGkeProvider::class, project)
        }, project).gcpGke()

    fun gcpGke(action: Action<in GcpGkeProvider>) = action.execute(gcpGke)

    val onPremise: OnPremiseProvider =
        DefaultOperatorProviderContainer(project.container(Provider::class) {
            project.objects.newInstance(OnPremiseProvider::class, project)
        }, project).onPremise()

    fun onPremise(action: Action<in OnPremiseProvider>) = action.execute(onPremise)

    val vmwareOpenshift: VmwareOpenshiftProvider =
        DefaultOperatorProviderContainer(project.container(Provider::class) {
            project.objects.newInstance(VmwareOpenshiftProvider::class, project)
        }, project).vmwareOpenshift()

    fun vmwareOpenshift(action: Action<in VmwareOpenshiftProvider>) = action.execute(vmwareOpenshift)

}
