package ai.digital.integration.server.util

import org.apache.commons.io.FileUtils
import org.gradle.api.Project

class CopyBuildArtifactsUtil {

    static def execute(Project project, Map<String, String> copyBuildArtifacts, String workingDir) {
        copyBuildArtifacts.each { Map.Entry<String, String> entry ->
            String where = entry.key
            String whatPattern = entry.value

            FileUtil.findFiles(project.buildDir.absolutePath, whatPattern, /\/[^\/]*integration-server\/[^\/]*/).each { File file ->
                FileUtils.copyFile(file, new File("${workingDir}/${where}/${file.name}"))
            }
        }
    }
}
