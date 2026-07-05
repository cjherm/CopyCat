package gui

import java.io.File
import java.util.Properties

data class CopyCatSettingsExport(
    val sourceDirs: List<File>,
    val compareDirs: List<File>?,
    val destDirs: List<File>,
    val includeTypes: List<String>
)

object CopyCatSettingsExportStore {

    fun save(file: File, settings: CopyCatSettingsExport) {
        val properties = Properties()
        properties.setProperty("compareActive", (settings.compareDirs != null).toString())
        properties.setDirs("source", settings.sourceDirs)
        properties.setDirs("compare", settings.compareDirs.orEmpty())
        properties.setDirs("dest", settings.destDirs)
        properties.setStrings("includeType", settings.includeTypes)

        file.outputStream().use { properties.store(it, "CopyCat settings") }
    }
}
