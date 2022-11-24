package ai.digital.integration.server.common.util

import ai.digital.integration.server.deploy.internals.DeployServerUtil
import org.gradle.api.Project
import java.nio.file.Path

class CacheUtil {

    companion object {

        fun isCacheEnabled(project: Project): Boolean {
            return PropertyUtil.resolveBooleanValue(project, "useCache", false)
        }

        fun getBaseDirectory(project: Project): String {
            return FileUtil.toPathString(DeployServerUtil.getServerDistFolderPath(project), "cache")
        }

        fun getComposeFileRelativePath(): String {
            return "cache/docker-compose_infinispan.yaml"
        }

        fun getResolvedDockerFile(project: Project): Path {
            val resultComposeFilePath = DockerComposeUtil.getResolvedDockerPath(project, getComposeFileRelativePath())

            val cacheTemplate = resultComposeFilePath.toFile()
            val port = 11222

            val resolvedCachePort = PropertyUtil.resolveIntValue(project, "cachePort", port)

            val configuredTemplate = cacheTemplate.readText()
                    .replace("CACHE_PORT", "${resolvedCachePort}:${port}")

            cacheTemplate.writeText(configuredTemplate)

            return resultComposeFilePath
        }
    }
}