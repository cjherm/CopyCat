package app

import java.io.File

class CopyCatConfiguration {

    var uniqueFiles = mapOf<String, List<File>>()
    var sourceDir = File("")
    var compareDir = File("")
    var copyDestDir = File("")
    var filesSelectedToBeCopied = listOf<File>()
}