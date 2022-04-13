package ai.digital.integration.server.deploy.tasks.cli

import ai.digital.integration.server.common.cluster.operator.OperatorHelper
import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.common.util.XlCliUtil
import ai.digital.integration.server.deploy.internals.DeployConfigurationsUtil.Companion.XL_CLI_DIST
import ai.digital.integration.server.deploy.internals.DeployExtensionUtil
import ai.digital.integration.server.release.internals.ReleaseExtensionUtil
import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Copy

open class DownloadXlCliDistTask : DefaultTask() {

    companion object {
        const val NAME = "downloadXlCliDist"
    }

    init {
        this.group = PLUGIN_GROUP

        if (DeployExtensionUtil.getExtension(project).clusterProfiles.operator().activeProviderName.isPresent || ReleaseExtensionUtil.getExtension(project).clusterProfiles.operator().activeProviderName.isPresent) {

            val operatorHelper = OperatorHelper.getOperatorHelper(project)
            val profile = operatorHelper.getProfile()

            val taskName = "xlCliExec"
            if (profile.xlCliPath.isPresent) {
                val path = profile.xlCliPath.get()

                if (path.startsWith("http")) {
                    this.dependsOn(project.tasks.register(taskName, Download::class.java) {
                        src(path)
                        dest(XlCliUtil.localDir(project))
                    })
                } else {
                    this.dependsOn(project.tasks.register(taskName, Copy::class.java) {
                        from(path)
                        into(XlCliUtil.localDir(project))
                    })
                }
            } else if (profile.xlCliVersion.isPresent) {
                val version = profile.xlCliVersion.get()
                project.logger.lifecycle("Downloading XL cli ${version}.")
                if (profile.cliNightly.get()) {
                    project.buildscript.dependencies.add(
                        XL_CLI_DIST,
                        "com.xebialabs.xlclient:xl-client:${version}:${XlCliUtil.osFolder}@bin"
                    )
                    val fromFile = project.buildscript.configurations.getByName(XL_CLI_DIST).singleFile
                    this.dependsOn(project.tasks.register(taskName, Copy::class.java) {
                        from(fromFile)
                        into(XlCliUtil.localDir(project))
                        rename(fromFile.name, "xl")
                    })
                } else {
                    this.dependsOn(project.tasks.register(taskName, Download::class.java) {
                        src(XlCliUtil.distUrl(version))
                        dest(XlCliUtil.localDir(project))
                    })
                }
            }
        } else {
            project.logger.warn("Active provider name is not set - DownloadXlCliDistTask")
        }
    }
}
