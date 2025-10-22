package app

import exceptions.UserWantsToQuitProgramException
import user.UserInteraction.Answer
import utility.FileHelper
import java.io.File
import java.io.IOException

class CopyCatApplication {

    fun getSrcAndTargetDirectoriesFromUser(config: CopyCatConfiguration) {
        var srcDir = File("")
        var compareDir = File("")
        var userAnswer = Answer.NO
        while (userAnswer == Answer.NO) {
            srcDir = FileHelper.promptForValidDirectory("Enter the path to the source directory:")
            compareDir =
                FileHelper.promptForValidDirectory("Enter the path to the directory in which to search for duplicates:")

            println("\t    Source directory:\n\t\t${srcDir.absolutePath}")
            println("\tComparison directory:\n\t\t${compareDir.absolutePath}")

            userAnswer = FileHelper.askQuestionAndRequestAnswer("Is this correct?")
            if (userAnswer == Answer.QUIT) {
                throw UserWantsToQuitProgramException()
            }
        }
        println("Counting all files...")
        config.sourceDir = srcDir
        config.compareDir = compareDir
        val filesInSrcDir = FileHelper.countFilesRecursively(config.sourceDir)
        val filesInCompareDir = FileHelper.countFilesRecursively(config.compareDir)
        println("\t$filesInSrcDir files in \"${config.sourceDir.absolutePath}\"")
        println("\t$filesInCompareDir files in \"${config.compareDir.absolutePath}\"")
    }

    fun createUniqueFilesLists(config: CopyCatConfiguration) {
        val userAnswer = FileHelper.askQuestionAndRequestAnswer("Start to search for unique files in source directory?")
        if (userAnswer != Answer.YES) {
            throw UserWantsToQuitProgramException()
        }
        println("Starting search...")
        val uniqueFilesList = FileHelper.findMissingFilesGroupedByType(config.sourceDir, config.compareDir)
        config.uniqueFiles = uniqueFilesList
        FileHelper.printAllTypesOfUniqueFiles(uniqueFilesList)
    }

    fun letUserSelectFileTypesToBeCopied(config: CopyCatConfiguration) {
        var userAnswer = Answer.UNDEFINED
        var selectedFileTypes = listOf<String>()
        while (userAnswer != Answer.YES) {
            println("Please select what file types should be copied like this: jpg png or [Q/q] to quit")
            val enteredLine = readLine()
            if (enteredLine != null) {
                val trimmedLine = enteredLine.trim()
                if (trimmedLine.lowercase() == "q") {
                    throw UserWantsToQuitProgramException()
                }
                selectedFileTypes = extractAndFilterStrings(trimmedLine, config.uniqueFiles.keys)
                println("Your selection is: $selectedFileTypes")
                userAnswer = FileHelper.askQuestionAndRequestAnswer("Is this selection correct?")
            }
        }
        config.filesSelectedToBeCopied = collectFilesForKeys(selectedFileTypes, config.uniqueFiles)
    }

    private fun collectFilesForKeys(keys: List<String>, map: Map<String, List<File>>): List<File> {
        return keys
            .mapNotNull { key -> map[key] }  // Get the file list for each key (if present)
            .flatten()                       // Combine all lists into one
    }

    private fun extractAndFilterStrings(input: String, allowedSet: Set<String>): List<String> {
        return input
            .split("\\s+".toRegex())         // Split by whitespace
            .filter { it.isNotBlank() }      // Ignore blanks
            .distinct()                      // Remove duplicates
            .filter { it in allowedSet }     // Keep only allowed strings
            .sorted()                        // Sort result
    }

    fun copyFilesWithProgress(config: CopyCatConfiguration) {
        println("Starting copy process...")
        val files = config.filesSelectedToBeCopied
        val sourceDir = config.sourceDir
        val destDir = config.copyDestDir

        if (!destDir.exists()) {
            destDir.mkdirs()
        }

        val totalFiles = files.size
        if (totalFiles == 0) {
            println("No files to copy.")
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
                    println("Copied $copiedCount / $totalFiles files...")
                }

            } catch (e: IOException) {
                println("Failed to copy ${file.name}: ${e.message}")
                failedCount++
            } catch (e: IllegalArgumentException) {
                println("Path error for ${file.absolutePath}: ${e.message}")
                failedCount++
            }
        }

        println("Finished copying.")
        println("Copied files: $copiedCount")
        println("Skipped files (already existed): $skippedCount")
        println("Failed to copy due to error: $failedCount")
    }

    fun letUserSelectTempDirectory(config: CopyCatConfiguration) {
        config.copyDestDir =
            FileHelper.promptForDirectory("Enter the path to the separate directory:")
    }

    fun showWelcome() {
        println("Welcome to CopyCat!")
        println("This little helper will copy all files from a source directory which are new compared to the files in a directory of your choosing!")
        println("Let's get started, shall we?")
        println("***************************************************")
    }
}