package gui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import utility.FileHelper
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
private fun DirList(title: String, dirs: List<File>) {
    Text(title, style = MaterialTheme.typography.subtitle1)
    dirs.forEach { Text("   ${it.absolutePath}") }
}

@Composable
fun SummaryScreen(
    sourceDirs: List<File>,
    compareDirs: List<File>?,
    destDirs: List<File>,
    selectedFiles: List<File>,
    onBack: () -> Unit,
    onLaunch: () -> Unit
) {
    val includeTypes = remember(selectedFiles) {
        selectedFiles.map { it.extension.lowercase().ifBlank { FileHelper.NO_EXTENSION_KEY } }.distinct().sorted()
    }
    val totalSize = remember(selectedFiles) { selectedFiles.sumOf { it.length() } }
    val smallestFreeSpace = remember(destDirs) { destDirs.minOfOrNull { it.usableSpaceOfNearestExistingAncestor() } }

    var isLaunching by remember { mutableStateOf(false) }
    var launchMessage by remember { mutableStateOf<String?>(null) }
    var saveMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Summary", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        DirList("Source directories:", sourceDirs)
        Spacer(modifier = Modifier.height(8.dp))

        if (compareDirs != null) {
            DirList("Compare directories:", compareDirs)
        } else {
            Text("Comparing against the destination directories.", style = MaterialTheme.typography.subtitle1)
        }
        Spacer(modifier = Modifier.height(8.dp))

        DirList("Destination directories:", destDirs)
        Spacer(modifier = Modifier.height(16.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))

        Text("File types to copy: ${includeTypes.joinToString(", ")}")
        Text("Files to copy: ${selectedFiles.size} (${formatBytes(totalSize)})")
        if (smallestFreeSpace != null) {
            Text("Smallest free space among destination directories: ${formatBytes(smallestFreeSpace)}")
        }

        Spacer(modifier = Modifier.height(24.dp))
        Row {
            Button(onClick = onBack, enabled = !isLaunching) {
                Text("Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                val chooser = JFileChooser()
                chooser.dialogTitle = "Save settings"
                chooser.fileFilter = FileNameExtensionFilter("CopyCat settings (*.config)", "config")
                chooser.selectedFile = File("CopyCat.config")
                if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                    val chosen = chooser.selectedFile
                    val target = if (chosen.extension.equals("config", ignoreCase = true)) {
                        chosen
                    } else {
                        File(chosen.parentFile, "${chosen.name}.config")
                    }
                    CopyCatSettingsExportStore.save(
                        target,
                        CopyCatSettingsExport(sourceDirs, compareDirs, destDirs, includeTypes)
                    )
                    saveMessage = "Settings saved to ${target.absolutePath}"
                }
            }) {
                Text("Save settings...")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    isLaunching = true
                    launchMessage = "Copying files, please wait..."
                    onLaunch()
                    launchMessage = "Copy finished. Check the log for details."
                },
                enabled = !isLaunching
            ) {
                Text("LAUNCH")
            }
        }

        saveMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it)
        }
        launchMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, style = MaterialTheme.typography.subtitle1)
        }
    }
}
