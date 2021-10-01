package ai.digital.integration.server.util

import ai.digital.integration.server.IntegrationServerExtension
import ai.digital.integration.server.domain.Server
import groovy.io.FileType
import org.gradle.api.Project

import java.nio.file.Path
import java.nio.file.Paths

class ServerUtil {

    static Server getServer(Project project) {
        def ext = project.extensions.getByType(IntegrationServerExtension)
        def server = ext.servers.first()
        server.setDebugPort(getDebugPort(project, server))
        server.setHttpPort(getHttpPort(project, server))
        server.setVersion(getServerVersion(project, server))

        if (server.dockerImage?.trim()) {
            server.setRuntimeDirectory(null)
        }

        if (!server.contextRoot.startsWith("/")) {
            server.contextRoot = "/$server.contextRoot"
        }

        server
    }

    private static String getServerVersion(Project project, Server server) {
        project.hasProperty("xlDeployVersion") ? project.property("xlDeployVersion") : server.version
    }

    private static Integer getHttpPort(Project project, Server server) {
        project.hasProperty("serverHttpPort") ? Integer.valueOf(project.property("serverHttpPort").toString()) : server.httpPort
    }

    private static Integer getDebugPort(Project project, Server server) {
        if (PropertyUtil.resolveBooleanValue(project, "debug", true)) {
            PropertyUtil.resolveIntValue(project, "serverDebugPort", server.debugPort)
        } else {
            null
        }
    }

    private static def dockerServerRelativePath() {
        "deploy/server-docker-compose.yaml"
    }

    static def isDockerBased(Project project) {
        getServer(project).dockerImage?.trim()
    }

    static String getHttpHost() {
        "localhost"
    }

    static String getUrl(Project project) {
        def server = getServer(project)
        def hostName = getHttpHost()
        if (isTls(project)) {
            "https://$hostName:${server.httpPort}${server.contextRoot}"
        } else {
            "http://$hostName:${server.httpPort}${server.contextRoot}"
        }
    }

    static boolean isTls(Project project) {
        getServer(project).tls
    }

    static boolean isAkkaSecured(Project project) {
        getServer(project).akkaSecured
    }

    static Boolean isServerDefined(Project project) {
        def ext = project.extensions.getByType(IntegrationServerExtension)
        ext.servers.size() > 0
    }

    static def isDistDownloadRequired(Project project) {
        getServer(project).runtimeDirectory == null && !isDockerBased(project)
    }

    static Path getResolvedDockerFile(Project project) {
        def server = getServer(project)
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

    static Path getRelativePathInIntegrationServerDist(Project project, String relativePath) {
        Paths.get("${IntegrationServerUtil.getDist(project)}/${relativePath}")
    }

    static def getServerDistFolderPath(Project project) {
        Paths.get(IntegrationServerUtil.getDist(project))
    }

    static def waitForBoot(Project project, Process process) {
        def server = getServer(project)
        def url = "${getUrl(project)}/deployit/metadata/type"
        WaitForBootUtil.byPort(project, "Deploy", url, server.httpPort, process)
    }

    static def getDockerImageVersion(Project project) {
        def server = getServer(project)
        "${server.dockerImage}:${server.version}"
    }

    static def getDockerServiceName(Project project) {
        def server = getServer(project)
        "deploy-${server.version}"
    }

    static String getServerWorkingDir(Project project) {
        Server server = getServer(project)

        if (isDockerBased(project)) {
            def workDir = getRelativePathInIntegrationServerDist(project, "deploy")
            workDir.toAbsolutePath().toString()
        } else if (server.runtimeDirectory == null) {
            def targetDir = getServerDistFolderPath(project).toString()
            Paths.get(targetDir, "xl-deploy-${server.version}-server").toAbsolutePath().toString()
        } else {
            def target = project.projectDir.toString()
            Paths.get(target, server.runtimeDirectory).toAbsolutePath().toString()
        }
    }

    static void grantPermissionsToIntegrationServerFolder(Project project) {
        if (isDockerBased(project)) {
            def workDir = IntegrationServerUtil.getDist(project)
            new File(workDir).traverse(type: FileType.ANY) { File it ->
                FileUtil.grantRWPermissions(it)
            }
        }
    }

    static def readDeployitConfProperty(Project project, String key) {
        def deployitConf = Paths.get("${getServerWorkingDir(project)}/conf/deployit.conf").toFile()
        PropertiesUtil.readProperty(deployitConf, key)
    }

    static def getLogDir(Project project) {
        Paths.get(getServerWorkingDir(project), "log").toFile()
    }

    static def createDebugString(Boolean debugSuspend, Integer debugPort) {
        def suspend = debugSuspend ? 'y' : 'n'
        "-Xrunjdwp:transport=dt_socket,server=y,suspend=${suspend},address=${debugPort}"
    }

    static def startServerFromClasspath(Project project) {
        project.logger.lifecycle("startServerFromClasspath.")
        Server server = getServer(project)
        def classpath = project.configurations.getByName(ConfigurationsUtil.DEPLOY_SERVER).filter { !it.name.endsWith("-sources.jar") }.asPath

        project.logger.lifecycle("Launching Deploy Server from classpath ${classpath}.")
        project.logger.lifecycle("Starting integration test server on port ${server.httpPort} from runtime dir ${server.runtimeDirectory}")

        def params = [
            fork: true,
            dir: server.runtimeDirectory,
            spawn: server.stdoutFileName == null,
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
