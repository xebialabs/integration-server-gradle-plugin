package ai.digital.integration.server.common.util

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
        fun copyDirs(srcBaseDir: String, targetBaseDir: String, dirs: List<String>) {
            dirs.forEach { dir: String ->
                FileUtils.copyDirectory(
                    Paths.get(srcBaseDir, dir).toFile(),
                    Paths.get(targetBaseDir, dir).toFile()
                )
            }
        }

        fun copyFiles(srcDir: String, targetDir: String, files: List<String>) {
            files.forEach { file ->
                FileUtils.copyFileToDirectory(
                    Paths.get(srcDir, file).toFile(),
                    Paths.get(targetDir).toFile()
                )
            }
        }

        fun copyFile(source: InputStream, dest: Path) {
            val parentDir = dest.parent.toFile()
            if (!parentDir.exists()) {
                parentDir.mkdirs()
            }
            val destFile = dest.toFile()
            destFile.createNewFile()
            val os = FileOutputStream(destFile)

            os.use {
                IOUtils.copy(source, it)
            }
        }

        fun pathToString(path: Path): String {
            return path.toAbsolutePath().toString()
        }

        fun toPathString(path: Path, subDir: String): String {
            return pathToString(Paths.get(pathToString(path), subDir))
        }

        fun grantRWPermissions(file: File) {
            file.setWritable(true, false)
            file.setReadable(true, false)
        }

        fun findFiles(basedir: String, pattern: String): List<File> {
            return FileNameByRegexFinder().getFileNames(basedir, pattern).map { File(it) }
        }

        fun findFiles(basedir: String, pattern: String, excludesPattern: String): List<File> {
            return FileNameByRegexFinder().getFileNames(basedir, pattern, excludesPattern).map { File(it) }
        }

        fun removeEmptyLines(data: String, output: File) {
            output.writeText("")
            data
                .lines().map { line -> line.trimEnd() }
                .filter { line -> line.isNotEmpty() }
                .forEach { line -> output.appendText("$line\n") }
        }
    }

}
