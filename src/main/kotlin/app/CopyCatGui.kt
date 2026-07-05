package app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import config.CopyCatConfiguration
import gui.DirectorySelectionScreen
import gui.FileTypeSelectionScreen
import gui.LastDirectorySettings
import gui.LastDirectorySettingsStore
import utility.Logger
import java.io.File
import kotlin.system.exitProcess

private sealed class GuiScreen {
    data class DirectorySelection(val sourceDirs: List<File>, val compareDirs: List<File>?, val destDirs: List<File>) : GuiScreen()
    data class FileTypeSelection(val sourceDirs: List<File>, val compareDirs: List<File>?, val destDirs: List<File>) : GuiScreen()
}

class CopyCatGui(config: CopyCatConfiguration, skipLastSettings: Boolean) {
    init {
        val initialSettings = if (skipLastSettings) null else LastDirectorySettingsStore.load()

        application {
            val windowState = rememberWindowState(width = 900.dp, height = 700.dp)
            Window(onCloseRequest = ::exitApplication, title = "CopyCat", state = windowState) {
                var screen by remember {
                    mutableStateOf<GuiScreen>(
                        if (initialSettings != null) {
                            GuiScreen.DirectorySelection(initialSettings.sourceDirs, initialSettings.compareDirs, initialSettings.destDirs)
                        } else {
                            GuiScreen.DirectorySelection(config.sourceDirs, null, config.copyDestDirs)
                        }
                    )
                }

                when (val currentScreen = screen) {
                    is GuiScreen.DirectorySelection -> DirectorySelectionScreen(
                        initialSourceDirs = currentScreen.sourceDirs,
                        initialCompareDirs = currentScreen.compareDirs,
                        initialDestDirs = currentScreen.destDirs
                    ) { sourceDirs, compareDirs, destDirs ->
                        LastDirectorySettingsStore.save(LastDirectorySettings(sourceDirs, compareDirs, destDirs))
                        screen = GuiScreen.FileTypeSelection(sourceDirs, compareDirs, destDirs)
                    }

                    is GuiScreen.FileTypeSelection -> FileTypeSelectionScreen(
                        sourceDirs = currentScreen.sourceDirs,
                        compareDirs = currentScreen.compareDirs ?: currentScreen.destDirs,
                        destDirs = currentScreen.destDirs,
                        onBack = {
                            screen = GuiScreen.DirectorySelection(
                                currentScreen.sourceDirs,
                                currentScreen.compareDirs,
                                currentScreen.destDirs
                            )
                        },
                        onNext = { selectedFiles ->
                            Logger.info("Selected ${selectedFiles.size} files to copy")
                            // TODO: navigate to the next screen once it exists
                        }
                    )
                }
            }
        }

        // JFileChooser (used by the "Browse" buttons) starts the AWT event thread, which is non-daemon
        // and would otherwise keep the JVM alive after all windows are closed.
        exitProcess(0)
    }
}
