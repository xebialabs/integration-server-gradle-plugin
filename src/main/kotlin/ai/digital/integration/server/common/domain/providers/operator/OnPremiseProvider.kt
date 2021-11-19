package ai.digital.integration.server.common.domain.providers.operator

import org.gradle.api.Project
import org.gradle.api.tasks.Input
import javax.inject.Inject

@Suppress("UnstableApiUsage")
open class OnPremiseProvider @Inject constructor(@Input val name: String, project: Project) {
}
