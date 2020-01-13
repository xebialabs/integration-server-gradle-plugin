package com.xebialabs.gradle.integration.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

import static com.xebialabs.gradle.integration.util.PluginUtil.PLUGIN_GROUP

class PrepareDatabaseTask extends DefaultTask {
    static NAME = "prepareDatabase"

    private String databaseName() {
        project.hasProperty("database") ? project.property("database").toString() : "derby-inmemory"
    }

    private static String detectDbDependency(db) {
        switch (db) {
            case 'postgres': return 'org.postgresql:postgresql'
            case 'oracle-xe-11g': return 'com.oracle:ojdbc6'
            case 'db2': return 'com.ibm:db2jcc4'
            case ['mysql', 'mysql-8']: return 'mysql:mysql-connector-java'
            case 'mssql': return 'com.microsoft.sqlserver:mssql-jdbc'
            case 'derby-network': return 'org.apache.derby:derbyclient'
            case 'derby-inmemory': return 'org.apache.derby:derby'
            default: return null
        }
    }

    private void copyDatabaseConf() {
        def from = PrepareDatabaseTask.class.classLoader.getResourceAsStream("database-conf/xl-deploy.conf.${databaseName()}")
        def into = project.buildDir.toPath().resolve("resources").resolve("test").resolve("xl-deploy.conf").toFile()
        into.mkdirs()
        into.delete()
        into.createNewFile()
        into << from
        from.close()
    }

    private static void injectDbDependency(Project project, def dbName) {
        def testCompile = project.configurations.findByName("testCompile")
        if (testCompile) {
            def dbDependency = detectDbDependency(dbName)
            project.logger.debug("Test compile configuration has been found. Injecting database dependency '$dbDependency' for db: '$dbName'")
            project.dependencies.add(testCompile.name, detectDbDependency(dbName))
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
        def dbName = databaseName()
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
