package utility

import utility.ConsolePrinter.Companion.printGreen
import utility.ConsolePrinter.Companion.printRed
import utility.ConsolePrinter.Companion.printYellow
import java.io.File

class UserInteraction {

    companion object {

        fun promptForValidDirectory(prompt: String): File {
            while (true) {
                val inputPath = promptUser(prompt)
                if (inputPath.lowercase() == "q") throw UserWantsToQuitProgramException()
                val file = File(inputPath)

                when {
                    file.isValidDirectory() -> return file

                    file.exists() && file.isFile -> {
                        if (askToUseParentDirectory(file)) {
                            return File(file.parent)
                        }
                    }

                    else -> printRed("Path does not exist. Please try again.")
                }
            }
        }

        fun askQuestionAndRequestAnswer(prompt: String): Answer {
            var userAnswer = Answer.UNDEFINED
            while (userAnswer == Answer.UNDEFINED) {
                printGreen("\n$prompt [Y/N or Q to exit]")
                userAnswer = getAnswer()
            }
            return userAnswer
        }

        private fun getAnswer(): Answer {
            val typedIn = readlnOrNull()
            if (typedIn != null) {
                when (typedIn.lowercase()) {
                    "y" -> return Answer.YES
                    "n" -> return Answer.NO
                    "q" -> throw UserWantsToQuitProgramException()
                }
            }
            return Answer.UNDEFINED
        }

        private fun File.isValidDirectory(): Boolean = this.exists() && this.isDirectory

        private fun askToUseParentDirectory(file: File): Boolean {
            val parent = file.parent ?: return false
            printYellow("That path is a file.")
            val message = "Do you want to use the containing directory ($parent) instead? [Y/N or Q to exit]: "
            val answer = promptUser(message)
            if (answer.lowercase() == "q") throw UserWantsToQuitProgramException()
            return answer.equals("y", ignoreCase = true)
        }

        private fun promptUser(message: String): String {
            printGreen("\n$message ")
            return readlnOrNull()?.trim().orEmpty()
        }

        fun promptForDirectory(prompt: String): File {
            while (true) {
                val input = promptUser(prompt)
                if (input.lowercase() == "q") throw UserWantsToQuitProgramException()
                val dir = File(input)

                when {
                    !dir.exists() -> {
                        printGreen("\nDirectory does not exist. Create it? [Y/N or Q to exit]: ")
                        val createAnswer = readlnOrNull()?.trim().orEmpty()
                        if (createAnswer.lowercase() == "q") throw UserWantsToQuitProgramException()
                        if (createAnswer.equals("y", ignoreCase = true)) {
                            if (dir.mkdirs()) {
                                println("\nDirectory created.")
                                return dir
                            } else {
                                printRed("Failed to create directory. Try again.")
                            }
                        }
                    }

                    !dir.isDirectory -> {
                        printRed("The path is not a directory. Try again.")
                    }

                    dir.listFiles()?.isNotEmpty() == true -> {
                        printYellow("Directory is not empty. Use it anyway? [Y/N or Q to exit]: ")
                        val useAnswer = readlnOrNull()?.trim().orEmpty()
                        if (useAnswer.lowercase() == "q") throw UserWantsToQuitProgramException()
                        if (useAnswer.equals("y", ignoreCase = true)) {
                            return dir
                        }
                    }

                    else -> {
                        return dir
                    }
                }
            }
        }

        fun promptForBoolean(prompt: String): Boolean {
            val answer = askQuestionAndRequestAnswer(prompt)
            while (true) {
                return when (answer) {

                    Answer.YES -> {
                        true
                    }

                    Answer.NO -> {
                        false
                    }

                    Answer.QUIT -> {
                        throw UserWantsToQuitProgramException()
                    }

                    else -> promptForBoolean(prompt)
                }
            }
        }

        fun askQuestionAndRequestIntegerList(prompt: String): List<Int> {
            var userAnswer = mutableListOf<Int>()
            while (userAnswer.isEmpty()) {
                printGreen("\n$prompt [Q to exit]")
                userAnswer = getIntegerList()
            }
            return userAnswer
        }

        private fun getIntegerList(): MutableList<Int> {
            val input = readlnOrNull()?.trim().orEmpty()
            if (input.lowercase() == "q") throw UserWantsToQuitProgramException()
            return input
                .split("\\s+".toRegex())
                .filter { it.isNotBlank() }
                .mapNotNull { it.toIntOrNull() }
                .toMutableList()
        }

        fun printAllTypesOfUniqueFiles(uniqueFilesList: Map<String, List<File>>) {
            println("File types and their counts:")

            // Split the map into: regular entries and the special "(no extension)"
            val (regularEntries, noExtEntry) = uniqueFilesList.entries.partition { it.key != FileHelper.NO_EXTENSION_KEY }

            // Sort regular entries by file count descending
            val sortedEntries = regularEntries.sortedByDescending { it.value.size }

            // Combine sorted regular entries with "(no extension)" last
            val finalEntries = if (noExtEntry.isNotEmpty()) {
                sortedEntries + noExtEntry
            } else {
                sortedEntries
            }

            if (finalEntries.isEmpty()) {
                printRed("\tNo unique files found!")
                throw RestartProgramException()
            }

            // Calculate padding
            val maxKeyLength = finalEntries.maxOf { it.key.length }

            // Print each line with aligned colon and updated suffix
            for ((fileType, files) in finalEntries) {
                val paddedKey = fileType.padEnd(maxKeyLength)
                println("\t$paddedKey: ${files.size} file/s")
            }
        }
    }
}
