package ai.digital.integration.server.common.domain.profiles

import org.gradle.api.Project
import org.gradle.api.tasks.Input
import javax.inject.Inject

@Suppress("UnstableApiUsage")
open class HelmProfile @Inject constructor(@Input var name: String, project: Project) : OperatorHelmProfile(project)
