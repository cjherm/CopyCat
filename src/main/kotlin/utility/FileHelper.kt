package utility

import user.UserInteraction.Answer
import java.io.File

class FileHelper {

    companion object {

        private const val NO_EXTENSION_KEY = "(no extension)"

        fun promptForValidDirectory(prompt: String): File {
            while (true) {
                val inputPath = promptUser(prompt)
                val file = File(inputPath)

                when {
                    file.isValidDirectory() -> return file

                    file.exists() && file.isFile -> {
                        if (askToUseParentDirectory(file)) {
                            return File(file.parent)
                        }
                    }

                    else -> println("❌ Path does not exist. Please try again.")
                }
            }
        }

        fun askQuestionAndRequestAnswer(prompt: String): Answer {
            var userAnswer = Answer.UNDEFINED
            while (userAnswer == Answer.UNDEFINED) {
                println("$prompt [Y/y or N/n or Q/q to exit]")
                userAnswer = getAnswer()
            }
            return userAnswer
        }

        private fun getAnswer(): Answer {
            val typedIn = readLine()
            if (typedIn != null) {
                when (typedIn.lowercase()) {
                    "y" -> return Answer.YES
                    "n" -> return Answer.NO
                    "q" -> return Answer.QUIT
                }
            }
            return Answer.UNDEFINED
        }

        private fun File.isValidDirectory(): Boolean = this.exists() && this.isDirectory

        private fun askToUseParentDirectory(file: File): Boolean {
            val parent = file.parent ?: return false
            println("⚠️ That path is a file.")
            val message = "Do you want to use the containing directory ($parent) instead? [y/N]: "
            val answer = promptUser(message)
            return answer.equals("y", ignoreCase = true)
        }

        fun countFilesRecursively(dir: File): Int {
            val files = dir.listFiles() ?: return 0
            var count = 0

            for (file in files) {
                count += if (file.isFile) 1 else countFilesRecursively(file)
            }

            return count
        }

        private fun promptUser(message: String): String {
            print("$message ")
            return readLine()?.trim().orEmpty()
        }

        fun promptForDirectory(prompt: String): File {
            while (true) {
                val input = promptUser(prompt)
                val dir = File(input)

                when {
                    !dir.exists() -> {
                        print("Directory does not exist. Create it? [Y/y or N/n]: ")
                        if (readLine()?.trim().equals("y", ignoreCase = true)) {
                            if (dir.mkdirs()) {
                                println("Directory created.")
                                return dir
                            } else {
                                println("Failed to create directory. Try again.")
                            }
                        }
                    }

                    !dir.isDirectory -> {
                        println("The path is not a directory. Try again.")
                    }

                    dir.listFiles()?.isNotEmpty() == true -> {
                        print("Directory is not empty. Use it anyway? [Y/y or N/n]: ")
                        if (readLine()?.trim().equals("y", ignoreCase = true)) {
                            return dir
                        }
                    }

                    else -> {
                        return dir
                    }
                }
            }
        }

        fun printAllTypesOfUniqueFiles(uniqueFilesList: Map<String, List<File>>) {
            println("File types and their counts:")

            // Split the map into: regular entries and the special "(no extension)"
            val (regularEntries, noExtEntry) = uniqueFilesList.entries.partition { it.key != NO_EXTENSION_KEY }

            // Sort regular entries by file count descending
            val sortedEntries = regularEntries.sortedByDescending { it.value.size }

            // Combine sorted regular entries with "(no extension)" last
            val finalEntries = if (noExtEntry.isNotEmpty()) {
                sortedEntries + noExtEntry
            } else {
                sortedEntries
            }

            // Calculate padding
            val maxKeyLength = finalEntries.maxOf { it.key.length }

            // Print each line with aligned colon and updated suffix
            for ((fileType, files) in finalEntries) {
                val paddedKey = fileType.padEnd(maxKeyLength)
                println("\t$paddedKey: ${files.size} file/s")
            }
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
