package ai.digital.integration.server.tasks.database

import ai.digital.integration.server.util.DbUtil
import org.gradle.api.DefaultTask
import org.gradle.api.Project

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

class PrepareDatabaseTask extends DefaultTask {
    static NAME = "prepareDatabase"

    private static void injectDbDependency(Project project, def dbName) {
        def testCompile = project.configurations.findByName("testCompile")
        if (testCompile) {
            def dbDependency = DbUtil.detectDbDependencies(dbName).getDriverDependency()
            project.logger.debug("Test compile configuration has been found. Injecting database dependency '$dbDependency' for db: '$dbName'")
            project.dependencies.add(testCompile.name, dbDependency)
        }
    }

    private static void registerTestCompileHooks(Project project, def dbName) {
        def compileTestScala = project.tasks.findByName("compileTestScala")
        def test = project.tasks.findByName("test")

        if (compileTestScala) {
            compileTestScala.finalizedBy(NAME)
            if (dbName == 'derby-network' && test) {
                compileTestScala.dependsOn("derbyStart")
                test.finalizedBy("derbyStop")
            }
        }
    }

    PrepareDatabaseTask() {
        def dbName = DbUtil.databaseName(project)
        this.configure {
            group = PLUGIN_GROUP
            project.afterEvaluate {
                injectDbDependency(project, dbName)
                registerTestCompileHooks(project, dbName)
            }
        }
    }

}
