package ai.digital.integration.server.util

import groovy.util.FileNameByRegexFinder
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.file.Path
import java.nio.file.Paths

class FileUtil {

    companion object {
        @JvmStatic
        fun copyDirs(srcBaseDir: String, targetBaseDir: String, dirs: List<String>) {
            dirs.forEach { dir: String ->
                FileUtils.copyDirectory(
                    Paths.get(srcBaseDir, dir).toFile(),
                    Paths.get(targetBaseDir, dir).toFile()
                )
            }
        }

        @JvmStatic
        fun copyFiles(srcDir: String, targetDir: String, files: List<String>) {
            files.forEach { file ->
                FileUtils.copyFileToDirectory(
                    Paths.get(srcDir, file).toFile(),
                    Paths.get(targetDir).toFile()
                )
            }
        }

        @JvmStatic
        fun copyFile(source: InputStream, dest: Path) {
            val parentDir = dest.getParent().toFile()
            if (!parentDir.exists()) {
                parentDir.mkdirs()
            }
            val destFile = dest.toFile()
            destFile.createNewFile()
            val os = FileOutputStream(destFile)

            try {
                IOUtils.copy(source, os)
            } finally {
                os.close()
            }
        }

        @JvmStatic
        fun pathToString(path: Path): String {
            return path.toAbsolutePath().toString()
        }

        @JvmStatic
        fun toPathString(path: Path, subDir: String): String {
            return pathToString(Paths.get(pathToString(path), subDir))
        }

        @JvmStatic
        fun grantRWPermissions(file: File) {
            file.setWritable(true, false)
            file.setReadable(true, false)
        }

        @JvmStatic
        fun findFiles(basedir: String, pattern: String): List<File> {
            return FileNameByRegexFinder().getFileNames(basedir, pattern).map { File(it) }
        }

        @JvmStatic
        fun findFiles(basedir: String, pattern: String, excludesPattern: String): List<File> {
            return FileNameByRegexFinder().getFileNames(basedir, pattern, excludesPattern).map { File(it) }
        }

        @JvmStatic
        fun removeEmptyLines(data: String, output: File) {
            output.writeText("")
            data
                .lines().map { line -> line.stripTrailing() }
                .filter { line -> line.isNotEmpty() }
                .forEach { line -> output.appendText("$line\n") }
        }
    }

}
