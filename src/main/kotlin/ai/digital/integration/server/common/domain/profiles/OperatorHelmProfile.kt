package ai.digital.integration.server.common.domain.profiles

import ai.digital.integration.server.common.constant.OperatorHelmProviderName
import ai.digital.integration.server.common.domain.providers.*
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.container
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.property

@Suppress("UnstableApiUsage")
abstract class OperatorHelmProfile (name: String, project: Project) : Profile {

    @Input
    val activeProviderName = project.objects.property<String>().value(OperatorHelmProviderName.ON_PREMISE.providerName)

    val awsOpenshift: AwsOpenshiftProvider =
        DefaultProviderContainer(project.container(Provider::class) {
            project.objects.newInstance(AwsOpenshiftProvider::class, project)
        }, project).awsOpenshift()

    fun awsOpenshift(action: Action<in AwsOpenshiftProvider>) {
        action.execute(awsOpenshift)
    }

    val awsEks: AwsEksProvider =
        DefaultProviderContainer(project.container(Provider::class) {
            project.objects.newInstance(AwsEksProvider::class, project)
        }, project).awsEks()

    fun awsEks(action: Action<in AwsEksProvider>) = action.execute(awsEks)

    val azureAks: AzureAksProvider =
        DefaultProviderContainer(project.container(Provider::class) {
            project.objects.newInstance(AzureAksProvider::class, project)
        }, project).azureAks()

    fun azureAks(action: Action<in AzureAksProvider>) = action.execute(azureAks)

    val gcpGke: GcpGkeProvider =
        DefaultProviderContainer(project.container(Provider::class) {
            project.objects.newInstance(GcpGkeProvider::class, project)
        }, project).gcpGke()

    fun gcpGke(action: Action<in GcpGkeProvider>) = action.execute(gcpGke)

    val onPremise: OnPremiseProvider =
        DefaultProviderContainer(project.container(Provider::class) {
            project.objects.newInstance(OnPremiseProvider::class, project)
        }, project).onPremise()

    fun onPremise(action: Action<in OnPremiseProvider>) = action.execute(onPremise)

    val vmwareOpenshift: VmwareOpenshiftProvider =
        DefaultProviderContainer(project.container(Provider::class) {
            project.objects.newInstance(VmwareOpenshiftProvider::class, project)
        }, project).vmwareOpenshift()

    fun vmwareOpenshift(action: Action<in VmwareOpenshiftProvider>) = action.execute(vmwareOpenshift)

}
