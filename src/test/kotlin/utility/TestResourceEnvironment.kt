package utility

import java.io.File

object TestResourceEnvironment {

    private val canonicalStructure: Map<String, List<String>> = mapOf(
        "testSrc"  to listOf("fileA.txt", "fileB.txt", "fileC.abc"),
        "testDest" to listOf("fileB.txt", "fileC.abc"),
        "testComp" to listOf("fileB.txt")
    )

    private val resourcesDir: File
        get() = File("src/test/resources").also {
            check(it.isDirectory) { "Test resources directory not found at: ${it.absolutePath}" }
        }

    fun setup() {
        for ((dirName, fileNames) in canonicalStructure) {
            val dir = File(resourcesDir, dirName)
            dir.deleteRecursively()
            dir.mkdirs()
            fileNames.forEach { File(dir, it).createNewFile() }
        }
    }

    fun teardown() {
        for (dirName in canonicalStructure.keys) {
            File(resourcesDir, dirName).deleteRecursively()
        }
    }
}
