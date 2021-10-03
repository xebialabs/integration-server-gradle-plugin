package ai.digital.integration.server.util

import ai.digital.integration.server.domain.Server
import org.gradle.api.Project

class ServerUtil {

    static def waitForBoot(Project project, Process process) {
        def server = DeployServerUtil.getServer(project)
        def url = "http://localhost:${server.httpPort}${server.contextRoot}/deployit/metadata/type"
        WaitForBootUtil.byPort(project, "Deploy", url, server.httpPort, process)
    }

    static def startServerFromClasspath(Project project) {
        project.logger.lifecycle("startServerFromClasspath.")
        Server server = DeployServerUtil.getServer(project)
        def classpath = project.configurations.getByName(ConfigurationsUtil.DEPLOY_SERVER).filter { !it.name.endsWith("-sources.jar") }.asPath

        project.logger.lifecycle("Launching Deploy Server from classpath ${classpath}.")
        project.logger.lifecycle("Starting integration test server on port ${server.httpPort} from runtime dir ${server.runtimeDirectory}")

        def params = [
                fork     : true,
                dir      : server.runtimeDirectory,
                spawn    : server.stdoutFileName == null,
                classname: "com.xebialabs.deployit.DeployitBootstrapper"
        ]
        String jvmPath = project.properties['integrationServerJVMPath']
        if (jvmPath) {
            jvmPath = jvmPath + '/bin/java'
            params['jvm'] = jvmPath
            project.logger.lifecycle("Using JVM from location: ${jvmPath}")
        }

        project.ant.java(params) {
            arg(value: '-force-upgrades')
            server.jvmArgs.each {
                jvmarg(value: it)
            }

            env(key: "CLASSPATH", value: classpath)

            if (server.stdoutFileName) {
                redirector(
                        output: "${getLogDir(project)}/${server.stdoutFileName}"
                )
            }

            if (server.debugPort != null) {
                project.logger.lifecycle("Enabled debug mode on port ${server.debugPort}")
                jvmarg(value: "-Xdebug")
                jvmarg(value: DeployServerUtil.createDebugString(server.debugSuspend, server.debugPort))
            }
        }
    }
}
