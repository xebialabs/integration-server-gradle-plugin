package ai.digital.integration.server.common.util

import ai.digital.integration.server.common.domain.api.DriverDependencyAware
import ai.digital.integration.server.deploy.internals.DeployServerUtil
import ai.digital.integration.server.deploy.internals.WorkerUtil
import org.gradle.api.Project
import java.nio.file.Path

class CacheUtil {

    companion object {

        private const val DEFAULT_CACHE_PROVIDER: String = "infinispan"

        private val cacheDependenciesVersions: Map<String, String> =
                mutableMapOf(
                        DEFAULT_CACHE_PROVIDER to "14.0.24.Final"
                )
        private val infinispanCacheParameters: CacheParameters = CacheParameters(
                "org.infinispan:infinispan-jcache-remote"
        )

        fun isCacheEnabled(project: Project): Boolean {
            return PropertyUtil.resolveBooleanValue(project, "useCache", false) &&
                    (DeployServerUtil.getServers(project).size > 1 || WorkerUtil.hasWorkers(project))
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

        fun getCacheDependency(): CacheParameters {
            return infinispanCacheParameters
        }

        fun getCacheDependencyVersion(name: String): String? {
            return cacheDependenciesVersions[name]
        }

        fun getCacheProviderName(): String {
            return DEFAULT_CACHE_PROVIDER
        }
    }
}

data class CacheParameters(
        override val driverDependency: String
) : DriverDependencyAware(driverDependency)
