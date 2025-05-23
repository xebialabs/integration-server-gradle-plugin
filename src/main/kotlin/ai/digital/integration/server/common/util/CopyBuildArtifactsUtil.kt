package ai.digital.integration.server.common.util

import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import java.io.File

class CopyBuildArtifactsUtil {
    companion object {
        fun execute(project: Project, copyBuildArtifacts: Map<String, String>, workingDir: String) {
            copyBuildArtifacts.forEach { entry: Map.Entry<String, String> ->
                val where = entry.key
                val whatPattern = entry.value

                FileUtil.findFiles(
                    project.layout.buildDirectory.get().asFile.absolutePath,
                    whatPattern, "/[^/]*integration-server/[^/]*"
                ).forEach { file: File ->
                    FileUtils.copyFile(file, File("${workingDir}/${where}/${file.name}"))
                }
            }
        }
    }
}
