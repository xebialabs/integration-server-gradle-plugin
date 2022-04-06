package ai.digital.integration.server.common.domain.providers

import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.property
import javax.inject.Inject


@Suppress("UnstableApiUsage")
open class AwsOpenshiftProvider @Inject constructor(project: Project) : Provider(project) {
    @Input
    val apiServerURL = project.objects.property<String>()

    @Input
    val oauthHostName = project.objects.property<String>()
}
