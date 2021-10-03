package ai.digital.integration.server.util

import ai.digital.integration.server.IntegrationServerExtension
import ai.digital.integration.server.domain.Server
import groovy.io.FileType
import org.gradle.api.Project

import java.nio.file.Path
import java.nio.file.Paths

class ServerUtil {

    private static def dockerServerRelativePath() {
        "deploy/server-docker-compose.yaml"
    }

    static Boolean isServerDefined(Project project) {
        def ext = project.extensions.getByType(IntegrationServerExtension)
        ext.servers.size() > 0
    }

    static def isDistDownloadRequired(Project project) {
        DeployServerUtil.getServer(project).runtimeDirectory == null && !DeployServerUtil.isDockerBased(project)
    }

    static Path getResolvedDockerFile(Project project) {
        def server = DeployServerUtil.getServer(project)
        def resultComposeFilePath = DockerComposeUtil.getResolvedDockerPath(project, dockerServerRelativePath())

        def serverTemplate = resultComposeFilePath.toFile()

        def configuredTemplate = serverTemplate.text
                .replace('DEPLOY_SERVER_HTTP_PORT', server.httpPort.toString())
                .replace('DEPLOY_IMAGE_VERSION', getDockerImageVersion(project))
                .replace('DEPLOY_PLUGINS_TO_EXCLUDE', server.defaultOfficialPluginsToExclude.join(","))
                .replace('DEPLOY_VERSION', server.version)
        serverTemplate.text = configuredTemplate

        return resultComposeFilePath
    }

    static def getServerDistFolderPath(Project project) {
        Paths.get(IntegrationServerUtil.getDist(project))
    }

    static def waitForBoot(Project project, Process process) {
        def server = DeployServerUtil.getServer(project)
        def url = "http://localhost:${server.httpPort}${server.contextRoot}/deployit/metadata/type"
        WaitForBootUtil.byPort(project, "Deploy", url, server.httpPort, process)
    }

    static def getDockerImageVersion(Project project) {
        def server = DeployServerUtil.getServer(project)
        "${server.dockerImage}:${server.version}"
    }

    static def getDockerServiceName(Project project) {
        def server = DeployServerUtil.getServer(project)
        "deploy-${server.version}"
    }

    static void grantPermissionsToIntegrationServerFolder(Project project) {
        if (DeployServerUtil.isDockerBased(project)) {
            def workDir = IntegrationServerUtil.getDist(project)
            new File(workDir).traverse(type: FileType.ANY) { File it ->
                FileUtil.grantRWPermissions(it)
            }
        }
    }

    static def readDeployitConfProperty(Project project, String key) {
        def deployitConf = Paths.get("${DeployServerUtil.getServerWorkingDir(project)}/conf/deployit.conf").toFile()
        PropertiesUtil.readProperty(deployitConf, key)
    }

    static def getLogDir(Project project) {
        Paths.get(DeployServerUtil.getServerWorkingDir(project), "log").toFile()
    }

    static def createDebugString(Boolean debugSuspend, Integer debugPort) {
        def suspend = debugSuspend ? 'y' : 'n'
        "-Xrunjdwp:transport=dt_socket,server=y,suspend=${suspend},address=${debugPort}"
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
                jvmarg(value: createDebugString(server.debugSuspend, server.debugPort))
            }
        }
    }
}
