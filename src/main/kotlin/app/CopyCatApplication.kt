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

        if (!config.configIsValid) {
            logError("Invalid Configuration! Cannot execute program!\n$config", config)
            return
        }

        logInfo("\nStarting copy process...", config)
        val files = config.filesSelectedToBeCopied
        val sourceDirs = config.sourceDirs
        val destDirs = config.copyDestDirs

        destDirs.forEach { destDir ->
            if (!destDir.exists()) {
                destDir.mkdirs()
            }
        }

        if (files.isEmpty()) {
            logWarn("No files to copy.", config)
            return
        }

        val totalOperations = files.size * destDirs.size
        val logStep = if (totalOperations > 10) totalOperations / 10 else totalOperations
        var copiedCount = 0
        var skippedCount = 0
        var failedCount = 0

        files.forEach { file ->
            val sourceDir = sourceDirs.firstOrNull { file.canonicalPath.startsWith(it.canonicalPath + File.separator) }
            if (sourceDir == null) {
                logError("Could not determine source directory for ${file.absolutePath}", config)
                failedCount += destDirs.size
                return@forEach
            }
            val relativePath = file.relativeTo(sourceDir).path

            destDirs.forEach destDirLoop@{ destDir ->
                try {
                    val destFile = File(destDir, relativePath)

                    if (destFile.exists()) {
                        logError(
                            "Skipped ${file.name}: \"${destFile.absolutePath}\" already exists.",
                            config
                        )
                        skippedCount++
                        return@destDirLoop
                    }

                    // Create necessary subdirectories
                    destFile.parentFile?.mkdirs()

                    // Copy the file
                    file.copyTo(destFile, overwrite = false)
                    copiedCount++

                    if ((totalOperations <= 10 && copiedCount == totalOperations) ||
                        (totalOperations > 10 && copiedCount % logStep == 0)
                    ) {
                        logInfo("Copied $copiedCount / $totalOperations files...", config)
                    }

                } catch (e: IOException) {
                    logError("Failed to copy ${file.name} to ${destDir.absolutePath}: ${e.message}", config)
                    failedCount++
                } catch (e: IllegalArgumentException) {
                    logError("Path error for ${file.absolutePath}: ${e.message}", config)
                    failedCount++
                }
            }
        }

        logInfo("Finished copying.", config)
        logInfo("\n$copiedCount files copied", config)
        logInfo("$skippedCount files skipped (already existed in destination)", config)
        logInfo("$failedCount files failed to copy\n", config)
    }

    fun logInfo(msg: String, cfg: CopyCatConfiguration) {
        if (cfg.printToConsole) {
            printWhite(msg)
        }
        if (cfg.printToFile) {
            Logger.info(msg)
        }
    }

    fun logWarn(msg: String, cfg: CopyCatConfiguration) {
        if (cfg.printToConsole) {
            printYellow(msg)
        }
        if (cfg.printToFile) {
            Logger.warn(msg)
        }
    }

    fun logError(msg: String, cfg: CopyCatConfiguration) {
        if (cfg.printToConsole) {
            printRed(msg)
        }
        if (cfg.printToFile) {
            Logger.error(msg)
        }
    }
}