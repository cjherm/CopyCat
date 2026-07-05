package gui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.io.File
import javax.swing.JFileChooser

private class DirectoryRowState(initialPath: String = "") {
    var path by mutableStateOf(initialPath)
}

private sealed class DirStatus {
    data object Blank : DirStatus()
    data object Valid : DirStatus()
    data class Invalid(val reason: String) : DirStatus()
}

private data class DirCheckResult(val status: DirStatus, val canonicalPath: String?)

// mustExist: false lets destination directories be picked even if they don't exist yet, since they get created on demand
private fun checkDirectorySelf(path: String, mustExist: Boolean, requireNonEmpty: Boolean): DirCheckResult {
    val trimmed = path.trim()
    if (trimmed.isEmpty()) return DirCheckResult(DirStatus.Blank, null)

    val dir = File(trimmed)
    if (dir.exists() && !dir.isDirectory) {
        return DirCheckResult(DirStatus.Invalid("Path points to a file, not a directory"), null)
    }
    if (mustExist && !dir.isDirectory) {
        return DirCheckResult(DirStatus.Invalid("Directory does not exist"), null)
    }
    if (requireNonEmpty && dir.isDirectory && dir.listFiles().isNullOrEmpty()) {
        return DirCheckResult(DirStatus.Invalid("Directory is empty"), null)
    }

    val canonicalPath = runCatching { dir.canonicalPath }.getOrDefault(dir.absolutePath)
    return DirCheckResult(DirStatus.Valid, canonicalPath)
}

// Flags a directory as invalid if it is the same as, or nests with, any other valid entry across all sections
private fun applyConflictChecks(results: List<DirCheckResult>): List<DirStatus> {
    val validEntries = results.withIndex().filter { it.value.status == DirStatus.Valid }

    return results.mapIndexed { index, result ->
        if (result.status != DirStatus.Valid) return@mapIndexed result.status

        val mine = result.canonicalPath!!
        val conflicts = validEntries.any { (otherIndex, other) ->
            val otherCanonical = other.canonicalPath
            otherIndex != index && otherCanonical != null && (
                mine == otherCanonical ||
                    mine.startsWith(otherCanonical + File.separator) ||
                    otherCanonical.startsWith(mine + File.separator)
                )
        }

        if (conflicts) DirStatus.Invalid("Overlaps with another selected directory") else DirStatus.Valid
    }
}

private fun groupIsValid(statuses: List<DirStatus>): Boolean =
    statuses.any { it == DirStatus.Valid } && statuses.none { it is DirStatus.Invalid }

@Composable
private fun DirectoryRow(
    row: DirectoryRowState,
    status: DirStatus,
    canRemove: Boolean,
    onRemove: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = row.path,
                onValueChange = { row.path = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                isError = status is DirStatus.Invalid,
                placeholder = { Text("Enter a directory path") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                val chooser = JFileChooser(row.path.trim().ifBlank { null })
                chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    row.path = chooser.selectedFile.absolutePath
                }
            }) {
                Text("Browse")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onRemove, enabled = canRemove) {
                Text("-")
            }
        }
        if (status is DirStatus.Invalid) {
            Text(status.reason, color = MaterialTheme.colors.error, style = MaterialTheme.typography.caption)
        }
    }
}

@Composable
private fun DirectorySection(
    title: String,
    rows: SnapshotStateList<DirectoryRowState>,
    statuses: List<DirStatus>
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(title, style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.height(4.dp))
        rows.forEachIndexed { index, row ->
            DirectoryRow(
                row = row,
                status = statuses.getOrElse(index) { DirStatus.Blank },
                canRemove = rows.size > 1,
                onRemove = { rows.removeAt(index) }
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        Button(onClick = { rows.add(DirectoryRowState()) }) {
            Text("+ Add directory")
        }
    }
}

@Composable
fun DirectorySelectionScreen(
    initialSourceDirs: List<File>,
    initialCompareDirs: List<File>?,
    initialDestDirs: List<File>,
    onNext: (sourceDirs: List<File>, compareDirs: List<File>?, destDirs: List<File>) -> Unit
) {
    val sourceRows = remember {
        mutableStateListOf<DirectoryRowState>().apply {
            initialSourceDirs.ifEmpty { listOf(File("")) }.forEach { add(DirectoryRowState(it.path)) }
        }
    }
    val destRows = remember {
        mutableStateListOf<DirectoryRowState>().apply {
            initialDestDirs.ifEmpty { listOf(File("")) }.forEach { add(DirectoryRowState(it.path)) }
        }
    }
    val compareRows = remember {
        mutableStateListOf<DirectoryRowState>().apply {
            (initialCompareDirs?.ifEmpty { listOf(File("")) } ?: listOf(File(""))).forEach { add(DirectoryRowState(it.path)) }
        }
    }
    var compareActive by remember { mutableStateOf(initialCompareDirs != null) }

    val sourceSelf = sourceRows.map { checkDirectorySelf(it.path, mustExist = true, requireNonEmpty = true) }
    val compareSelf = if (compareActive) {
        compareRows.map { checkDirectorySelf(it.path, mustExist = true, requireNonEmpty = true) }
    } else {
        emptyList()
    }
    val destSelf = destRows.map { checkDirectorySelf(it.path, mustExist = false, requireNonEmpty = false) }

    val combinedStatuses = applyConflictChecks(sourceSelf + compareSelf + destSelf)
    val sourceStatuses = combinedStatuses.take(sourceSelf.size)
    val compareStatuses = combinedStatuses.drop(sourceSelf.size).take(compareSelf.size)
    val destStatuses = combinedStatuses.drop(sourceSelf.size + compareSelf.size)

    val sourceValid = groupIsValid(sourceStatuses)
    val destValid = groupIsValid(destStatuses)
    val compareValid = !compareActive || groupIsValid(compareStatuses)
    val canProceed = sourceValid && destValid && compareValid

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Select directories", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        DirectorySection(title = "Source directories", rows = sourceRows, statuses = sourceStatuses)

        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = compareActive, onCheckedChange = { compareActive = it })
            Text("Use separate directories for comparison")
        }
        if (compareActive) {
            DirectorySection(title = "Compare directories", rows = compareRows, statuses = compareStatuses)
        }

        Spacer(modifier = Modifier.height(8.dp))
        DirectorySection(title = "Destination directories", rows = destRows, statuses = destStatuses)

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                val sourceDirs = sourceRows.filter { it.path.isNotBlank() }.map { File(it.path.trim()) }
                val destDirs = destRows.filter { it.path.isNotBlank() }.map { File(it.path.trim()) }
                val compareDirs = if (compareActive) {
                    compareRows.filter { it.path.isNotBlank() }.map { File(it.path.trim()) }
                } else {
                    null
                }
                onNext(sourceDirs, compareDirs, destDirs)
            },
            enabled = canProceed
        ) {
            Text("Next")
        }
    }
}
