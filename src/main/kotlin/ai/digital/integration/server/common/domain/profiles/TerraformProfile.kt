package ai.digital.integration.server.common.domain.profiles

import ai.digital.integration.server.common.constant.TerraformProviderName
import ai.digital.integration.server.common.domain.providers.terraform.AwsEksProvider
import ai.digital.integration.server.common.domain.providers.terraform.DefaultTerraformProviderContainer
import ai.digital.integration.server.common.domain.providers.terraform.Provider
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.container
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

@Suppress("UnstableApiUsage")
open class TerraformProfile @Inject constructor(@Input val name: String, project: Project) : Profile {
    @Input
    val activeProviderName = project.objects.property<String>().value(TerraformProviderName.AWS_EKS.providerName)

    val awsEks: AwsEksProvider =
        DefaultTerraformProviderContainer(project.container(Provider::class) {
            project.objects.newInstance(AwsEksProvider::class, project)
        }, project).awsEks()

    fun awsEks(action: Action<in AwsEksProvider>) = action.execute(awsEks)
}
