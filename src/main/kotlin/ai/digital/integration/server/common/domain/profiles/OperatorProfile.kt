package ai.digital.integration.server.common.domain.profiles

import ai.digital.integration.server.common.constant.OperatorProviderName
import ai.digital.integration.server.common.domain.providers.operator.*
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.property

@Suppress("UnstableApiUsage")
open class OperatorProfile(objects: ObjectFactory) {
    var activeProviderName = objects.property<String>().value(OperatorProviderName.ON_PREMISE.providerName).get()

    var awsEksProvider: AwsEksProvider =
        objects.property<AwsEksProvider>().value(AwsEksProvider(objects)).get()

    var awsOpenshiftProvider: AwsOpenshiftProvider =
        objects.property<AwsOpenshiftProvider>().value(AwsOpenshiftProvider(objects)).get()

    var azureAksProvider: AzureAksProvider =
        objects.property<AzureAksProvider>().value(AzureAksProvider(objects)).get()

    var gcpGkeProvider: GcpGkeProvider =
        objects.property<GcpGkeProvider>().value(GcpGkeProvider(objects)).get()

    var onPremiseProvider: OnPremiseProvider =
        objects.property<OnPremiseProvider>().value(OnPremiseProvider(objects)).get()

    var vmwareOpenshiftProvider: VmwareOpenshiftProvider =
        objects.property<VmwareOpenshiftProvider>().value(VmwareOpenshiftProvider(objects)).get()
}
