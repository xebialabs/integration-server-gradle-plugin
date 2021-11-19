package ai.digital.integration.server.common.domain.providers.operator

import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.property
import javax.inject.Inject


@Suppress("UnstableApiUsage")
open class AwsOpenshiftProvider @Inject constructor(project: Project) : Provider {
    @Input
    val name = project.objects.property<String>()

    @Input
    val host = project.objects.property<String>()
}
