package app

import java.io.File

class CopyCatConfiguration(
    val sourceDir: File,
    val copyDestDir: File,
    val filesSelectedToBeCopied: List<File>,
)