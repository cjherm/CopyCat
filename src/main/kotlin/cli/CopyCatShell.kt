package cli

import config.CopyCatConfigurationBuilder
import utility.Answer
import utility.UserWantsToQuitProgramException
import utility.ConsolePrinter.Companion.printRed
import utility.ConsolePrinter.Companion.printWhite
import utility.ConsolePrinter.Companion.printYellow
import utility.FileHelper
import java.io.File

class CopyCatShell {

    fun getSrcAndTargetDirectoriesFromUser(config: CopyCatConfigurationBuilder) {
        var srcDir = File("")
        var compareDir = File("")
        var promptForCompareDir = true
        var userAnswer = Answer.NO
        while (userAnswer == Answer.NO) {
            srcDir = FileHelper.promptForValidDirectory("Enter the path to the source directory:")
            while (promptForCompareDir) {
                compareDir =
                    FileHelper.promptForValidDirectory("Enter the path to the directory in which to search for duplicates:")
                promptForCompareDir = false
                if (compareDir == srcDir) {
                    printRed("\tYou cannot use the source directory here!")
                    promptForCompareDir = true
                }
            }
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

    fun createUniqueFilesLists(config: CopyCatConfigurationBuilder) {
        val userAnswer = FileHelper.askQuestionAndRequestAnswer("Start to search for unique files in source directory?")
        if (userAnswer != Answer.YES) {
            throw UserWantsToQuitProgramException()
        }
        println("Starting search...")
        val uniqueFilesList = FileHelper.findMissingFilesGroupedByType(config.sourceDir, config.compareDir)
        config.uniqueFiles = uniqueFilesList
        FileHelper.printAllTypesOfUniqueFiles(uniqueFilesList)
    }

    fun letUserSelectFileTypesToBeCopied(config: CopyCatConfigurationBuilder) {
        var userAnswer = Answer.UNDEFINED
        var selectedFileTypes = listOf<String>()
        while (userAnswer != Answer.YES) {
            println("Please select what file types should be copied like this: jpg png or [$] for all or [Q/q] to quit")
            val enteredLine = readlnOrNull()
            if (enteredLine != null) {
                val trimmedLine = enteredLine.trim()
                if (trimmedLine.lowercase() == "q") {
                    throw UserWantsToQuitProgramException()
                }
                selectedFileTypes = if (trimmedLine == "$") {
                    config.uniqueFiles.keys.toList()
                } else {
                    extractAndFilterStrings(trimmedLine, config.uniqueFiles.keys)
                }
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

    fun letUserSelectTempDirectory(config: CopyCatConfigurationBuilder) {
        config.useSeparateDestDir =
            FileHelper.promptForBoolean("Do you want to use a separate destination directory? If not, then CopyCat will use this one?: ${config.compareDir.absolutePath}")
        if (config.useSeparateDestDir) {
            config.copyDestDir =
                FileHelper.promptForDirectory("Enter the path to the separate directory:")
        } else {
            config.copyDestDir = config.compareDir
        }
    }

    fun showWelcome() {
        printWhite("\n***************************************************")
        printYellow("\n\tWelcome to CopyCat!\n")
        println(
            "This little helper will copy all files from\n" +
                    "a source directory which are new compared to\n" +
                    "the files in a directory of your choosing!"
        )
        println("Let's get started, shall we?\n")
        println("***************************************************")
    }
}
