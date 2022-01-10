package ai.digital.integration.server.common.domain.profiles

import org.gradle.api.Project
import javax.inject.Inject

@Suppress("UnstableApiUsage")
open class XlBlueprintProfile @Inject constructor(project: Project) : Profile {

    val name: String = "xlBlueprint"

}
