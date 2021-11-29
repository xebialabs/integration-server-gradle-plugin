package ai.digital.integration.server.common.domain.providers.operator

import org.gradle.api.Project
import javax.inject.Inject


@Suppress("UnstableApiUsage")
open class AwsOpenshiftProvider @Inject constructor(project: Project) : Provider(project) {

}
