package ai.digital.integration.server.deploy.internals

import org.apache.commons.io.FileUtils
import java.io.File

class CopyDeployServerUtil {
    companion object {
        fun execute(copyFolders: Map<String, List<File>>, workingDir: String) {
            copyFolders.forEach { entry ->
                val where = entry.key
                val listOfDirectories = entry.value

                listOfDirectories.forEach { directory ->
                    FileUtils.copyDirectory(directory, File("${workingDir}/${where}"))
                }
            }
        }
    }
}
