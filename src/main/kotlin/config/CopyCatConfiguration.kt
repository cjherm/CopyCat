package config

import java.io.File

class CopyCatConfiguration(
    val sourceDir: File,
    val copyDestDir: File,
    val filesSelectedToBeCopied: List<File>,
    val printToConsole: Boolean,
    val printToFile: Boolean
) {
    override fun toString(): String {
        return "CopyCat Configuration:\n" +
                "                            \tSource directory: ${sourceDir.absolutePath}\n" +
                "                            \tDestination:      ${copyDestDir.absolutePath}\n" +
                "                            \tFiles to copy:    ${filesSelectedToBeCopied.size}" +
                "                            \tprintToConsole:   $printToConsole\n" +
                "                            \tprintToFile:      $printToFile\n"
    }
}