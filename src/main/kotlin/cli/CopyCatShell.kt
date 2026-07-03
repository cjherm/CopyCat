package cli

import config.CopyCatConfigurationBuilder
import utility.Answer
import utility.ConsolePrinter.Companion.printGreen
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
                    printRed("You cannot use the source directory here!")
                    promptForCompareDir = true
                }
            }
            println("\n    Source directory: ${srcDir.absolutePath}")
            println("Comparison directory: ${compareDir.absolutePath}")

            // in case we reloop this part because user answered it is not correct
            promptForCompareDir = true

            userAnswer = FileHelper.askQuestionAndRequestAnswer("Is this correct?")
            if (userAnswer == Answer.QUIT) {
                throw UserWantsToQuitProgramException()
            }
        }
        println("\nCounting all files...")
        // TODO Fix this pseudo fixes
        config.sourceDir = listOf(srcDir)
        config.compareDir = listOf(compareDir)
        val filesInSrcDir = FileHelper.countFilesRecursively(config.sourceDir[0])
        val filesInCompareDir = FileHelper.countFilesRecursively(config.compareDir[0])
        println("\t$filesInSrcDir file/s in \"${config.sourceDir[0].absolutePath}\"")
        println("\t$filesInCompareDir file/s in \"${config.compareDir[0].absolutePath}\"")
    }

    fun createUniqueFilesLists(config: CopyCatConfigurationBuilder) {
        println("\nStart to searching for unique files in source directory...")
        // TODO Fix this pseudo fix
        val uniqueFilesList = FileHelper.findMissingFilesGroupedByType(config.sourceDir[0], config.compareDir[0])
        config.uniqueFiles = uniqueFilesList
        FileHelper.printAllTypesOfUniqueFiles(uniqueFilesList)
    }

    fun letUserSelectFileTypesToBeCopied(config: CopyCatConfigurationBuilder) {
        var userAnswer = Answer.UNDEFINED
        var selectedFileTypes = listOf<String>()
        while (userAnswer != Answer.YES) {
            printGreen("\nPlease select what file types should be copied like this: jpg png or $ for all or Q to quit")
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
                println("\nYour selection is: $selectedFileTypes")
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
        // TODO Fix this pseudo fix
        config.useSeparateDestDir =
            FileHelper.promptForBoolean("Do you want to use a separate destination directory?\nIf not, then CopyCat will use this one:\n${config.compareDir[0].absolutePath}")
        if (config.useSeparateDestDir) {
            // TODO Fix this pseudo fix
            config.copyDestDir =
                listOf(FileHelper.promptForDirectory("Enter the path to the separate directory:"))
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

    fun letUserDecideOnLogFile(config: CopyCatConfigurationBuilder) {
        config.printToFile =
            FileHelper.promptForBoolean("Do you want to create a log file in the destination directory?")
    }
}