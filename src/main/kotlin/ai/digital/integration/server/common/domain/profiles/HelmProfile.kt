package ai.digital.integration.server.common.domain.profiles

import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

@Suppress("UnstableApiUsage")
open class HelmProfile @Inject constructor(@Input var name: String, project: Project) : OperatorHelmProfile(name, project) {

    @Input
    val ingressType = project.objects.property<String>().value(IngressType.NGINX.name)

    @Input
    val doCleanup = project.objects.property<Boolean>().value(true)

}
