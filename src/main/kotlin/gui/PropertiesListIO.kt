package gui

import java.io.File
import java.util.Properties

// Stores an indexed list under "prefix.count", "prefix.0", "prefix.1", ... avoiding delimiter-escaping issues
// for values (like directory paths) that could contain almost any character.

fun Properties.setDirs(prefix: String, dirs: List<File>) {
    setStrings(prefix, dirs.map { it.path })
}

fun Properties.getDirs(prefix: String): List<File> =
    getStrings(prefix).map { File(it) }

fun Properties.setStrings(prefix: String, values: List<String>) {
    setProperty("$prefix.count", values.size.toString())
    values.forEachIndexed { index, value -> setProperty("$prefix.$index", value) }
}

fun Properties.getStrings(prefix: String): List<String> {
    val count = getProperty("$prefix.count")?.toIntOrNull() ?: 0
    return (0 until count).mapNotNull { index -> getProperty("$prefix.$index") }
}
