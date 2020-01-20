package com.xebialabs.gradle.integration.tasks

import com.xebialabs.gradle.integration.util.DbUtil
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

import java.nio.file.Files

import static com.xebialabs.gradle.integration.util.PluginUtil.PLUGIN_GROUP

class PrepareDatabaseTask extends DefaultTask {
    static NAME = "prepareDatabase"

    private void copyDatabaseConf() {
        def dbname = DbUtil.databaseName(project)
        def from = PrepareDatabaseTask.class.classLoader.getResourceAsStream("database-conf/xl-deploy.conf.${dbname}")
        def intoDir = project.rootDir.toPath().resolve("src").resolve("test").resolve("resources")
        if (Files.exists(intoDir)) {
            def into = intoDir.resolve("xl-deploy.conf").toFile()
            into.delete()
            into.createNewFile()
            into << from
            from.close()
        }
    }

    private static void injectDbDependency(Project project, def dbName) {
        def testCompile = project.configurations.findByName("testCompile")
        if (testCompile) {
            def dbDependency = DbUtil.detectDbDependency(dbName).getDriverDependency()
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

    @TaskAction
    def prepare() {
        copyDatabaseConf()
    }
}
