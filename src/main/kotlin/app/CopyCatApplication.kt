package app

import config.CopyCatConfiguration
import utility.ConsolePrinter.Companion.printRed
import utility.ConsolePrinter.Companion.printWhite
import utility.ConsolePrinter.Companion.printYellow
import utility.Logger
import java.io.File
import java.io.IOException

class CopyCatApplication {
    fun launch(config: CopyCatConfiguration) {
        printAndLogInfo("\nStarting copy process...", config)
        val files = config.filesSelectedToBeCopied
        val sourceDir = config.sourceDir
        val destDir = config.copyDestDir

        if (!destDir.exists()) {
            destDir.mkdirs()
        }

        val totalFiles = files.size
        if (totalFiles == 0) {
            printAndLogWarn("No files to copy.", config)
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
                    printAndLogInfo("Copied $copiedCount / $totalFiles files...", config)
                }

            } catch (e: IOException) {
                printAndLogError("Failed to copy ${file.name}: ${e.message}", config)
                failedCount++
            } catch (e: IllegalArgumentException) {
                printAndLogError("Path error for ${file.absolutePath}: ${e.message}", config)
                failedCount++
            }
        }

        printAndLogInfo("Finished copying.", config)
        printAndLogInfo("\n                 Copied files: $copiedCount", config)
        printAndLogInfo("Skipped files (already existed): $skippedCount", config)
        printAndLogInfo("    Failed to copy due to error: $failedCount\n", config)
    }

    fun printAndLogInfo(msg: String, cfg: CopyCatConfiguration) {
        if (cfg.printToConsole) {
            printWhite(msg)
        }
        if (cfg.printToFile) {
            Logger.info(msg)
        }
    }

    fun printAndLogWarn(msg: String, cfg: CopyCatConfiguration) {
        if (cfg.printToConsole) {
            printYellow(msg)
        }
        if (cfg.printToFile) {
            Logger.warn(msg)
        }
    }

    fun printAndLogError(msg: String, cfg: CopyCatConfiguration) {
        if (cfg.printToConsole) {
            printRed(msg)
        }
        if (cfg.printToFile) {
            Logger.error(msg)
        }
    }
}