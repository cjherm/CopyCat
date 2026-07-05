package config

import java.io.File

class CopyCatConfigurationBuilder {
    var uniqueFiles = mapOf<String, List<File>>()
    var sourceDir = emptyList<File>()
    var compareDir = emptyList<File>()
    var copyDestDir = emptyList<File>()
    var filesSelectedToBeCopied = listOf<File>()
    var useSeparateDestDir = true
    var printToConsole = false
    var printToFile = false

    fun build(): CopyCatConfiguration {
        return CopyCatConfiguration(
            sourceDir,
            copyDestDir,
            filesSelectedToBeCopied,
            printToConsole,
            printToFile,
            configIsValid = true
        )
    }
}
