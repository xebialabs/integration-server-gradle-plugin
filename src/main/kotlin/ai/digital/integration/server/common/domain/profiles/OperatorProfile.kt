package ai.digital.integration.server.common.domain.profiles

import ai.digital.integration.server.common.constant.OperatorProviderName
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

@Suppress("UnstableApiUsage")
open class OperatorProfile @Inject constructor(@Input val name: String, project: Project) : Profile {

    @Input
    val activeProviderName = project.objects.property<String>().value(OperatorProviderName.ON_PREMISE.providerName)

//    var awsEksProvider: AwsEksProvider =
//        objects.property<AwsEksProvider>().value(AwsEksProvider(objects)).get()
//
//    var awsOpenshift: AwsOpenshiftProvider = objects.property<AwsOpenshiftProvider>().get()

//    var azureAksProvider: AzureAksProvider =
//        objects.property<AzureAksProvider>().value(AzureAksProvider(objects)).get()
//
//    var gcpGkeProvider: GcpGkeProvider =
//        objects.property<GcpGkeProvider>().value(GcpGkeProvider(objects)).get()
//
//    var onPremiseProvider: OnPremiseProvider =
//        objects.property<OnPremiseProvider>().value(OnPremiseProvider(objects)).get()
//
//    var vmwareOpenshiftProvider: VmwareOpenshiftProvider =
//        objects.property<VmwareOpenshiftProvider>().value(VmwareOpenshiftProvider(objects)).get()

//    fun awsOpenshift(action: Action<in AwsOpenshiftProvider>) = action.execute(awsOpenshift)
}
