package app

import java.io.File

class CopyCatConfiguration(
    val sourceDir: File,
    val copyDestDir: File,
    val filesSelectedToBeCopied: List<File>,
) {
    override fun toString(): String {
        val fileList = if (filesSelectedToBeCopied.isEmpty()) {
            "\t\t(none)"
        } else {
            filesSelectedToBeCopied.joinToString("\n") { "\t\t${it.absolutePath}" }
        }
        return """
            |CopyCat Configuration
            |  Source directory   : ${sourceDir.absolutePath}
            |  Destination        : ${copyDestDir.absolutePath}
            |  Files to copy      : ${filesSelectedToBeCopied.size}
            |$fileList
        """.trimMargin()
    }
}