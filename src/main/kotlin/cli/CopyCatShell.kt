package cli

import config.CopyCatConfigurationBuilder
import utility.Answer
import utility.ConsolePrinter.Companion.printGreen
import utility.ConsolePrinter.Companion.printWhite
import utility.ConsolePrinter.Companion.printYellow
import utility.FileHelper
import utility.UserWantsToQuitProgramException
import java.io.File

class CopyCatShell {

    fun getSrcAndTargetDirectoriesFromUser(config: CopyCatConfigurationBuilder) {
        val srcString = "source"
        val srcDirs = mutableListOf<File>()

        val compString = "compare"
        val compareDirs = mutableListOf<File>()

        collectDirs(srcString, srcDirs, null)
        collectDirs(compString, compareDirs, srcDirs)

        printWhite("\nCounting all files...")
        config.sourceDir = srcDirs
        config.compareDir = compareDirs
        val filesInSrcDir = config.sourceDir.sumOf { FileHelper.countFilesRecursively(it) }
        val filesInCompareDir = config.compareDir.sumOf { FileHelper.countFilesRecursively(it) }
        printWhite("\t$filesInSrcDir files in ${srcDirs.size} source directories")
        printWhite("\t$filesInCompareDir files to compare to ${compareDirs.size} directories")
    }

    private fun collectDirs(
        dirTypeString: String,
        dirList: MutableList<File>,
        listToCheckAgainst: List<File>?
    ) {
        var selectionIsCorrect = Answer.NO
        while (selectionIsCorrect == Answer.NO || dirList.isEmpty()) {

            var addMoreDirs = Answer.YES
            while (addMoreDirs == Answer.YES) {
                val dirCandidate = FileHelper.promptForValidDirectory("Enter the path to the $dirTypeString directory:")
                if (!candidateIsNoDuplicate(dirList, dirCandidate)) {
                    // yellow message already printed inside candidateIsNoDuplicate
                } else if (listToCheckAgainst != null && !candidateIsNoDuplicate(listToCheckAgainst, dirCandidate)) {
                    // yellow message already printed inside candidateIsNoDuplicate
                } else {
                    dirList.add(dirCandidate)
                }
                addMoreDirs = FileHelper.askQuestionAndRequestAnswer("Do you want add another directory?")
            }

            printWhite(
                "\n" +
                        "Selected $dirTypeString directories:\n" +
                        "----------------------------" +
                        printDirs(dirList)
            )

            selectionIsCorrect = FileHelper.askQuestionAndRequestAnswer("Is this correct?")

            if (selectionIsCorrect == Answer.YES) {
                break
            }

            if (FileHelper.askQuestionAndRequestAnswer("Do you want to remove item/s from this list?") == Answer.YES) {
                val entriesToBeRemoved =
                    FileHelper.askQuestionAndRequestIntegerList("What entry do you want to remove?\nWhen you want to enter multiple entries, separate them with an empty space: \"X Y\"")
                removeEntriesFromList(entriesToBeRemoved, dirList)

                printWhite(
                    "\n" +
                            "Selected $dirTypeString directories:\n" +
                            "----------------------------" +
                            printDirs(dirList)
                )

                selectionIsCorrect = FileHelper.askQuestionAndRequestAnswer("Is this correct?")
            }
        }
    }

    private fun removeEntriesFromList(
        entriesToBeRemoved: List<Int>,
        entries: MutableList<File>
    ) {
        entriesToBeRemoved
            .distinct()
            .map { it - 1 }
            .filter { it in entries.indices }
            .sortedDescending()
            .forEach { entries.removeAt(it) }
    }

    private fun candidateIsNoDuplicate(
        dirs: List<File>,
        dirCandidate: File
    ): Boolean {
        val candidatePath = dirCandidate.canonicalPath
        val conflict = dirs.firstOrNull { existing ->
            val existingPath = existing.canonicalPath
            candidatePath == existingPath || candidatePath.startsWith(existingPath + File.separator)
        }
        if (conflict != null) {
            if (conflict.canonicalPath == candidatePath) {
                printYellow("\"${dirCandidate.absolutePath}\" is already in the list.")
            } else {
                printYellow("\"${dirCandidate.absolutePath}\" is a sub-directory of \"${conflict.absolutePath}\", which is already in the list.")
            }
        }
        return conflict == null
    }

    private fun printDirs(dirs: List<File>): String {
        if (dirs.isEmpty()) {
            return "\nno directories added\n"
        } else {
            var dirString = ""
            var index = 1
            for (dir in dirs) {
                dirString += "\n\t$index)\t${dir.absolutePath}"
                index++
            }
            return dirString
        }
    }

    fun createUniqueFilesLists(config: CopyCatConfigurationBuilder) {
        printWhite("\nStart to searching for unique files in source directory...\n")
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
                printWhite("\nYour selection is: $selectedFileTypes")
                userAnswer = FileHelper.askQuestionAndRequestAnswer("\nIs this selection correct?")
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
        printYellow("\n\tWelcome to CopyCat!\n")
        printWhite(
            "This little helper will copy all files from\n" +
                    "a source directory which are new compared to\n" +
                    "the files in a directory of your choosing!"
        )
        printWhite("Let's get started, shall we?\n")
        printWhite("********************************************")
    }

    fun letUserDecideOnLogFile(config: CopyCatConfigurationBuilder) {
        config.printToFile =
            FileHelper.promptForBoolean("Do you want to create a log file in the destination directory?")
    }
}