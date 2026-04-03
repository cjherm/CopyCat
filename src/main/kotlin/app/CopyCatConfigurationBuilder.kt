package app

import java.io.File

class CopyCatConfigurationBuilder {
    var uniqueFiles = mapOf<String, List<File>>()
    var sourceDir = File("")
    var compareDir = File("")
    var copyDestDir = File("")
    var filesSelectedToBeCopied = listOf<File>()
    var useSeparateDestDir = true

    fun build(): CopyCatConfiguration {
        return CopyCatConfiguration(
            sourceDir,
            copyDestDir,
            filesSelectedToBeCopied,
        )
    }
}