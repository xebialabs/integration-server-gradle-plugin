package ai.digital.integration.server.common.domain.profiles

import ai.digital.integration.server.common.constant.TerraformProviderName
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

@Suppress("UnstableApiUsage")
open class TerraformProfile @Inject constructor(@Input val name: String, project: Project) : Profile {
    @Input
    val activeProviderName = project.objects.property<String>().value(TerraformProviderName.AWS_EKS.providerName)

//    var awsEks: AwsEksProvider =
//        objects.property<AwsEksProvider>().value(AwsEksProvider(objects)).get()
}
