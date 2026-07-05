package gui

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import utility.FileHelper
import java.io.File

private data class FileTypeInfo(val type: String, val count: Int, val totalSizeBytes: Long)

private fun formatBytes(bytes: Long): String {
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
private fun File.usableSpaceOfNearestExistingAncestor(): Long {
    var dir = this.absoluteFile
    while (!dir.exists()) {
        dir = dir.parentFile ?: return 0L
    }
    return dir.usableSpace
}

@Composable
fun FileTypeSelectionScreen(
    sourceDirs: List<File>,
    compareDirs: List<File>,
    destDirs: List<File>,
    onBack: () -> Unit,
    onNext: (List<File>) -> Unit
) {
    val uniqueFilesByType = remember(sourceDirs, compareDirs) {
        FileHelper.findUniqueFilesGroupedByType(sourceDirs, compareDirs)
    }

    val fileTypes = remember(uniqueFilesByType) {
        uniqueFilesByType
            .map { (type, files) -> FileTypeInfo(type, files.size, files.sumOf { it.length() }) }
            .sortedByDescending { it.count }
    }

    val selectedTypes = remember(fileTypes) {
        mutableStateMapOf<String, Boolean>().apply {
            fileTypes.forEach { put(it.type, true) }
        }
    }

    val smallestFreeSpace = remember(destDirs) {
        destDirs.minOfOrNull { it.usableSpaceOfNearestExistingAncestor() }
    }

    val allSelected = selectedTypes.values.isNotEmpty() && selectedTypes.values.all { it }
    val selectedTotalSize = fileTypes.filter { selectedTypes[it.type] == true }.sumOf { it.totalSizeBytes }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Select file types", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(8.dp))

        if (smallestFreeSpace != null) {
            Text("Smallest free space among destination directories: ${formatBytes(smallestFreeSpace)}")
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (fileTypes.isEmpty()) {
            Text("No new files found to copy.")
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = allSelected,
                    onCheckedChange = { checked -> fileTypes.forEach { selectedTypes[it.type] = checked } },
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select all")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Total size of selected types: ${formatBytes(selectedTotalSize)}", style = MaterialTheme.typography.subtitle1)
            Spacer(modifier = Modifier.height(8.dp))
            Divider()

            val listScrollState = rememberScrollState()
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxSize().verticalScroll(listScrollState).padding(end = 12.dp)) {
                    fileTypes.forEach { info ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)) {
                            Checkbox(
                                checked = selectedTypes[info.type] == true,
                                onCheckedChange = { checked -> selectedTypes[info.type] = checked },
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(info.type, modifier = Modifier.weight(1f))
                            Text("${info.count} file(s)", modifier = Modifier.width(100.dp))
                            Text(formatBytes(info.totalSizeBytes), modifier = Modifier.width(100.dp))
                        }
                    }
                }
                VerticalScrollbar(
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                    adapter = rememberScrollbarAdapter(listScrollState)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row {
            Button(onClick = onBack) {
                Text("Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    val selectedFiles = selectedTypes.filterValues { it }.keys.flatMap { uniqueFilesByType[it].orEmpty() }
                    onNext(selectedFiles)
                },
                enabled = selectedTypes.values.any { it }
            ) {
                Text("Next")
            }
        }
    }
}
