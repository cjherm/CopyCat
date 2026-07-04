package utility

import java.io.File

class FileHelper {

    companion object {

        const val NO_EXTENSION_KEY = "(no extension)"

        fun countFilesRecursively(dir: File): Int {
            val files = dir.listFiles() ?: return 0
            var count = 0

            for (file in files) {
                count += if (file.isFile) 1 else countFilesRecursively(file)
            }

            return count
        }

        fun findMissingFilesGroupedByType(sourceDir: File, compareDir: File): Map<String, List<File>> {
            require(sourceDir.isDirectory && compareDir.isDirectory) { "Both inputs must be directories." }

            val sourceFiles = sourceDir.walkTopDown().filter { it.isFile }.toList()
            val targetFiles = compareDir.walkTopDown().filter { it.isFile }.toSet()

            val targetIndex = targetFiles.associateBy {
                Triple(it.name, it.extension.lowercase(), it.length())
            }

            val missingFiles = sourceFiles.filter { file ->
                val key = Triple(file.name, file.extension.lowercase(), file.length())
                key !in targetIndex
            }

            return missingFiles.groupBy { file ->
                file.extension.lowercase().ifBlank { NO_EXTENSION_KEY }
            }
        }
    }
}
