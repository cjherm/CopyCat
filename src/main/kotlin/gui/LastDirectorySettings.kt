package gui

import utility.AppLocation
import java.io.File
import java.util.Properties

data class LastDirectorySettings(
    val sourceDirs: List<File>,
    val compareDirs: List<File>?,
    val destDirs: List<File>
)

object LastDirectorySettingsStore {

    private val file = File(AppLocation.directory(), "lastSettings.config")

    fun save(settings: LastDirectorySettings) {
        val properties = Properties()
        properties.setProperty("compareActive", (settings.compareDirs != null).toString())
        writeDirs(properties, "source", settings.sourceDirs)
        writeDirs(properties, "compare", settings.compareDirs.orEmpty())
        writeDirs(properties, "dest", settings.destDirs)

        file.outputStream().use { properties.store(it, "CopyCat - last used directory settings") }
    }

    fun load(): LastDirectorySettings? {
        if (!file.isFile) return null

        val properties = Properties()
        file.inputStream().use { properties.load(it) }

        val compareActive = properties.getProperty("compareActive")?.toBoolean() ?: false
        val compareDirs = readDirs(properties, "compare")

        return LastDirectorySettings(
            sourceDirs = readDirs(properties, "source"),
            compareDirs = if (compareActive) compareDirs else null,
            destDirs = readDirs(properties, "dest")
        )
    }

    private fun writeDirs(properties: Properties, prefix: String, dirs: List<File>) {
        properties.setProperty("$prefix.count", dirs.size.toString())
        dirs.forEachIndexed { index, dir -> properties.setProperty("$prefix.$index", dir.path) }
    }

    private fun readDirs(properties: Properties, prefix: String): List<File> {
        val count = properties.getProperty("$prefix.count")?.toIntOrNull() ?: 0
        return (0 until count).mapNotNull { index -> properties.getProperty("$prefix.$index")?.let { File(it) } }
    }
}
