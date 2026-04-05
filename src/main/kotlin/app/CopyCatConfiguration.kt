package app

import java.io.File

class CopyCatConfiguration(
    val sourceDir: File,
    val copyDestDir: File,
    val filesSelectedToBeCopied: List<File>,
) {
    override fun toString(): String {
        return "CopyCat Configuration:\n" +
                "                            \tSource directory: ${sourceDir.absolutePath}\n" +
                "                            \tDestination:      ${copyDestDir.absolutePath}\n" +
                "                            \tFiles to copy:    ${filesSelectedToBeCopied.size}"
    }
}