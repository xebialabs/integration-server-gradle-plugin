package ai.digital.integration.server.common.tasks.database

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.common.util.DbUtil
import org.gradle.api.DefaultTask
import org.gradle.api.Project

abstract class PrepareDatabaseTask : DefaultTask() {

    companion object {
        const val NAME = "prepareDatabase"
    }

    init {
        val dbName = DbUtil.databaseName(project)
        this.group = PLUGIN_GROUP
        project.afterEvaluate {
            injectDbDependency(project, dbName)
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
