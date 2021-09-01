package ai.digital.integration.server.util

import groovy.xml.XmlUtil
import org.apache.commons.io.IOUtils

import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

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

    static def grantRWPermissions(File file) {
        file.setWritable(true, false)
        file.setReadable(true, false)
    }

    static def compress(File baseDir, List<File> files, File archive) {
        FileOutputStream fos = new FileOutputStream(archive)
        def zos = new ZipOutputStream(fos)

        try {
            for (file in files) {
                zos.putNextEntry(new ZipEntry(file.path.minus(baseDir)))
                zos << file.bytes
                zos.closeEntry()
            }
        } finally {
            zos.close()
        }
    }

    static def removeEmptyLines(String data, File output) {
        output.withWriter {writer ->
            data.lines()
                .map {line -> line.stripTrailing() }
                .filter {line -> !line.empty }
                .forEach {line -> writer.append(line).append('\n') }
        }
    }
}
