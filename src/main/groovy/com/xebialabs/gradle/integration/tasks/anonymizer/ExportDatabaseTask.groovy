package com.xebialabs.gradle.integration.tasks.anonymizer

import com.xebialabs.gradle.integration.util.ConfigurationsUtil
import com.xebialabs.gradle.integration.util.ExtensionsUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class ExportDatabaseTask extends DefaultTask {

    static NAME = "exportDatabase"

    private void startFromClasspath(){
        def extension = ExtensionsUtil.getExtension(project)
        def classpath = project.configurations.getByName(ConfigurationsUtil.INTEGRATION_TEST_SERVER).filter { !it.name.endsWith("-sources.jar") }.asPath
        logger.debug("Export Database application classpath: \n${classpath}")

        project.logger.lifecycle("Starting export database ")

        project.javaexec {
            main = "com.xebialabs.database.anonymizer.AnonymizerBootstrapper"
            environment "CLASSPATH", classpath
            workingDir extension.serverRuntimeDirectory
        }
    }

    @TaskAction
    def startup() {
        if (ExtensionsUtil.getExtension(project).serverRuntimeDirectory != null) {
            startFromClasspath()
        }
    }
}
