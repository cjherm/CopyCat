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

        fun calculateFilesToCopy(
            srcDirs: List<File>,
            compDirs: List<File>,
            inclTypes: List<String>,
            exclTypes: List<String>
        ): List<File> {
            val uniqueFiles = srcDirs
                .flatMap { srcDir -> compDirs.map { compDir -> findMissingFilesGroupedByType(srcDir, compDir) } }
                .flatMap { it.entries }
                .groupBy({ it.key }, { it.value })
                .mapValues { (_, lists) -> lists.flatten().distinctBy { Triple(it.name, it.extension.lowercase(), it.length()) } }

            return when {
                inclTypes.isNotEmpty() -> inclTypes.mapNotNull { uniqueFiles[it] }.flatten()
                exclTypes.isNotEmpty() -> uniqueFiles.filterKeys { it !in exclTypes }.values.flatten()
                else -> uniqueFiles.values.flatten()
            }
        }
    }
}
