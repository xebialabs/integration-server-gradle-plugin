package ai.digital.integration.server.common.domain.providers

import org.gradle.api.Project
import javax.inject.Inject


@Suppress("UnstableApiUsage")
open class VmwareOpenshiftProvider @Inject constructor(project: Project) : Provider(project) {
}
