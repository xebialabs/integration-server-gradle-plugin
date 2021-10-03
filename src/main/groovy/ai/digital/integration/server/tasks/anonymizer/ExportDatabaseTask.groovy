package ai.digital.integration.server.tasks.anonymizer

import ai.digital.integration.server.domain.Server
import ai.digital.integration.server.util.ConfigurationsUtil
import ai.digital.integration.server.util.DeployServerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class ExportDatabaseTask extends DefaultTask {

    public static String NAME = "exportDatabase"

    private void startFromClasspath(Server server) {
        def classpath = project.configurations.getByName(ConfigurationsUtil.DEPLOY_SERVER).filter { !it.name.endsWith("-sources.jar") }.asPath
        logger.debug("Exporting Database application classpath: \n${classpath}")

        project.logger.lifecycle("Starting to export the database ")

        project.javaexec {
            main = "com.xebialabs.database.anonymizer.AnonymizerBootstrapper"
            environment "CLASSPATH", classpath
            workingDir server.runtimeDirectory
        }
    }

    @TaskAction
    def startup() {
        def server = DeployServerUtil.getServer(project)
        project.logger.lifecycle("Exporting database for Deploy server.")

        if (server.runtimeDirectory != null) {
            startFromClasspath(server)
        }
    }
}
