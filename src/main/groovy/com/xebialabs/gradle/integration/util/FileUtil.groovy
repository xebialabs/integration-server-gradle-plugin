package com.xebialabs.gradle.integration.util

import org.apache.commons.io.IOUtils

import java.nio.file.Path
import java.nio.file.Paths

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

    static def pathToString(Path path) {
        path.toAbsolutePath().toString()
    }

    static def toPathString(Path path, String subDir) {
        pathToString(Paths.get(pathToString(path), subDir))
    }
}
