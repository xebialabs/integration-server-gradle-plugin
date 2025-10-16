package ai.digital.integration.server.common.tasks.database

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.common.util.DbUtil
import org.gradle.api.DefaultTask
import org.gradle.api.Project

open class PrepareDatabaseTask : DefaultTask() {

    companion object {
        const val NAME = "prepareDatabase"
    }

    init {
        this.group = PLUGIN_GROUP

        val dbName = DbUtil.databaseName(project)
        val configureDbDependency = {
            injectDbDependency(project, dbName)
        }

        if (project.state.executed) {
            configureDbDependency()
        } else {
            project.afterEvaluate {
                configureDbDependency()
            }
        }
    }

    private fun injectDbDependency(project: Project, dbName: String) {
        val testCompile = project.configurations.findByName("testCompile")

        testCompile?.let { it ->
            val dbDependency = DbUtil.detectDbDependencies(dbName).driverDependency
            project.logger.debug("Test compile configuration has been found. Injecting database dependency '$dbDependency' for db: '$dbName'")
            project.dependencies.add(it.name, dbDependency)
        }
    }

}
