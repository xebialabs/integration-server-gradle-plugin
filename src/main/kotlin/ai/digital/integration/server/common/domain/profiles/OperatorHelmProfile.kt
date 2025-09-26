package ai.digital.integration.server.common.domain.profiles

import ai.digital.integration.server.common.domain.providers.*
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.container
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.property

@Suppress("UnstableApiUsage")
abstract class OperatorHelmProfile (project: Project) : Profile {

    @Input
    val deploymentTimeoutSeconds = project.objects.property<Int>().value(900)

    @Input
    val activeProviderName = project.objects.property<String>()

    @Input
    val namespace = project.objects.property<String>()

    @Input
    val ingressType = project.objects.property<String>().value(IngressType.NGINX.name)

    @Input
    val doCleanup = project.objects.property<Boolean>().value(true)

    @Input
    val deploySuffix = project.objects.property<String>()

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
