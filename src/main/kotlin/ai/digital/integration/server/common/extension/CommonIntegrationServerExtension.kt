package ai.digital.integration.server.common.extension

import ai.digital.integration.server.common.constant.ClusterProfileName
import ai.digital.integration.server.common.domain.Cluster
import ai.digital.integration.server.common.domain.profiles.*
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.kotlin.dsl.container
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.property

abstract class CommonIntegrationServerExtension(val project: Project) {

    val clusterProfiles: ProfileContainer =
        DefaultProfileContainer(project.container(Profile::class) { name ->
            when (name) {
                ClusterProfileName.DOCKER_COMPOSE.profileName ->
                    project.objects.newInstance(DockerComposeProfile::class, project)
                ClusterProfileName.OPERATOR.profileName ->
                    project.objects.newInstance(OperatorProfile::class, name, project)
                ClusterProfileName.TERRAFORM.profileName ->
                    project.objects.newInstance(TerraformProfile::class, name, project)
                else ->
                    throw IllegalArgumentException("Profile name `$name` is not supported. Choose one of ${
                        ClusterProfileName.values().joinToString { profileEnum -> profileEnum.profileName }
                    }")
            }

        })

    fun clusterProfiles(action: Action<in ProfileContainer>) = action.execute(clusterProfiles)

    val cluster = project.objects.property<Cluster>().value(Cluster(project.objects))

    fun cluster(action: Action<in Cluster>) = action.execute(cluster.get())
}
