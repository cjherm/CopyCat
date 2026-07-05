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
        properties.setDirs("source", settings.sourceDirs)
        properties.setDirs("compare", settings.compareDirs.orEmpty())
        properties.setDirs("dest", settings.destDirs)

        file.outputStream().use { properties.store(it, "CopyCat - last used directory settings") }
    }

    fun load(): LastDirectorySettings? {
        if (!file.isFile) return null

        val properties = Properties()
        file.inputStream().use { properties.load(it) }

        val compareActive = properties.getProperty("compareActive")?.toBoolean() ?: false
        val compareDirs = properties.getDirs("compare")

        return LastDirectorySettings(
            sourceDirs = properties.getDirs("source"),
            compareDirs = if (compareActive) compareDirs else null,
            destDirs = properties.getDirs("dest")
        )
    }
}
