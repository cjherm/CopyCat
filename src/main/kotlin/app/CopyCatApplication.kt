package app

import utility.Logger
import java.io.File
import java.io.IOException

class CopyCatApplication {
    fun launch(config: CopyCatConfiguration) {
        Logger.info("Starting copy process...")
        val files = config.filesSelectedToBeCopied
        val sourceDir = config.sourceDir
        val destDir = config.copyDestDir

        if (!destDir.exists()) {
            destDir.mkdirs()
        }

        val totalFiles = files.size
        if (totalFiles == 0) {
            Logger.warn("No files to copy.")
            return
        }

        val logStep = if (totalFiles > 10) totalFiles / 10 else totalFiles
        var copiedCount = 0
        var skippedCount = 0
        var failedCount = 0

        files.forEachIndexed { _, file ->
            try {
                // Get relative path from sourceDir and build destination path
                val relativePath = file.relativeTo(sourceDir).path
                val destFile = File(destDir, relativePath)

                // Skip if file with same name and extension already exists
                val fileTypeMatches = file.extension.equals(destFile.extension, ignoreCase = true)
                val nameMatches = file.name.equals(destFile.name, ignoreCase = true)

                if (destFile.exists() && nameMatches && fileTypeMatches) {
                    skippedCount++
                    return@forEachIndexed
                }

                // Create necessary subdirectories
                destFile.parentFile?.mkdirs()

                // Copy the file
                file.copyTo(destFile, overwrite = false)
                copiedCount++

                if ((totalFiles <= 10 && copiedCount == totalFiles) ||
                    (totalFiles > 10 && copiedCount % logStep == 0)
                ) {
                    Logger.info("Copied $copiedCount / $totalFiles files...")
                }

            } catch (e: IOException) {
                Logger.error("Failed to copy ${file.name}: ${e.message}")
                failedCount++
            } catch (e: IllegalArgumentException) {
                Logger.error("Path error for ${file.absolutePath}: ${e.message}")
                failedCount++
            }
        }

        Logger.info("Finished copying.")
        Logger.info("Copied files: $copiedCount")
        Logger.info("Skipped files (already existed): $skippedCount")
        Logger.info("Failed to copy due to error: $failedCount")
    }
}