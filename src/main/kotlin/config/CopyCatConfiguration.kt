package config

import utility.Logger
import java.io.File

class CopyCatConfiguration(
    val sourceDirs: List<File>,
    val copyDestDirs: List<File>,
    val filesSelectedToBeCopied: List<File>,
    val printToConsole: Boolean,
    val printToFile: Boolean
) {
    override fun toString(): String {
        val srcPaths = sourceDirs.joinToString("\n\t\t\t\t\t\t\t\t") { it.absolutePath }
        val destPaths = copyDestDirs.joinToString("\n\t\t\t\t\t\t\t\t") { it.absolutePath }
        val logFilePath = Logger.logFile?.absolutePath ?: "none"
        return "CopyCat Configuration:\n" +
                "                            \tSource directories: $srcPaths\n" +
                "                            \tDestinations:       $destPaths\n" +
                "                            \tFiles to copy:      ${filesSelectedToBeCopied.size}\n" +
                "                            \tlogToConsole:       $printToConsole\n" +
                "                            \tlogToFile:          $printToFile\n" +
                "                            \tlogFile:            $logFilePath"
    }
}