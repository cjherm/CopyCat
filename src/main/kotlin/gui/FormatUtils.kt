package gui

import java.io.File

fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val units = listOf("KB", "MB", "GB", "TB")
    var value = bytes / 1024.0
    var unitIndex = 0
    while (value >= 1024 && unitIndex < units.size - 1) {
        value /= 1024
        unitIndex++
    }
    return "%.2f %s".format(value, units[unitIndex])
}

// Walks up to the nearest existing ancestor, since a destination directory may not exist yet (it gets created on demand)
fun File.usableSpaceOfNearestExistingAncestor(): Long {
    var dir = this.absoluteFile
    while (!dir.exists()) {
        dir = dir.parentFile ?: return 0L
    }
    return dir.usableSpace
}
