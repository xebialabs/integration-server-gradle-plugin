package ai.digital.integration.server.common.domain.providers.operator

import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

@Suppress("UnstableApiUsage")
abstract class Provider @Inject constructor(project: Project) {

    @Input
    val name = project.objects.property<String>()

    @Input
    val host = project.objects.property<String>()

    @Input
    var operatorImage = project.objects.property<String>().value("xebialabs/deploy-operator:1.2.0-openshift").get()

    @Input
    var operatorPackageVersion: String = project.objects.property<String>().value("1.0.0").get()
}
