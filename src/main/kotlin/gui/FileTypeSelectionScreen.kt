package gui

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import utility.FileHelper
import java.io.File

private data class FileTypeInfo(val type: String, val count: Int, val totalSizeBytes: Long)

private enum class SortCriterion(val label: String) {
    NAME("Name"),
    FILE_COUNT("File count"),
    SIZE("Size")
}

private fun List<FileTypeInfo>.sortedByCriterion(criterion: SortCriterion): List<FileTypeInfo> = when (criterion) {
    SortCriterion.NAME -> sortedBy { it.type }
    SortCriterion.FILE_COUNT -> sortedByDescending { it.count }
    SortCriterion.SIZE -> sortedByDescending { it.totalSizeBytes }
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

    val unsortedFileTypes = remember(uniqueFilesByType) {
        uniqueFilesByType.map { (type, files) -> FileTypeInfo(type, files.size, files.sumOf { it.length() }) }
    }

    var sortCriterion by remember(uniqueFilesByType) { mutableStateOf(SortCriterion.FILE_COUNT) }
    val fileTypes = remember(unsortedFileTypes, sortCriterion) {
        unsortedFileTypes.sortedByCriterion(sortCriterion)
    }

    // Keyed on the unsorted set, not the sorted "fileTypes" list, so changing the sort order doesn't reset selections
    val selectedTypes = remember(unsortedFileTypes) {
        mutableStateMapOf<String, Boolean>().apply {
            unsortedFileTypes.forEach { put(it.type, true) }
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
                Text("Sort by:")
                Spacer(modifier = Modifier.width(8.dp))
                SortCriterion.entries.forEach { criterion ->
                    Spacer(modifier = Modifier.width(4.dp))
                    if (criterion == sortCriterion) {
                        Button(onClick = { sortCriterion = criterion }) {
                            Text(criterion.label)
                        }
                    } else {
                        OutlinedButton(onClick = { sortCriterion = criterion }) {
                            Text(criterion.label)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
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
