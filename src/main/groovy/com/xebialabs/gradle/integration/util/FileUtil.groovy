package com.xebialabs.gradle.integration.util

import org.apache.commons.io.IOUtils
import org.gradle.api.Project

import java.nio.file.Path

class FileUtil {

    static def copyFile(InputStream source, Path dest) {
        def parentDir = dest.getParent().toFile()
        if (!parentDir.exists()) {
            parentDir.mkdirs()
        }
        def destFile = dest.toFile()
        destFile.createNewFile()
        def os = new FileOutputStream(destFile)

        try {
            IOUtils.copy(source, os)
        } finally {
            os.close()
        }
    }

    static def readCCValue(Project project, String fileName, String key) {
        def file = new File("${ExtensionsUtil.getServerWorkingDir(project)}/centralConfiguration/$fileName")
        return YamlFileUtil.readFileKey(file, key)
    }
}
