package app

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import config.CopyCatConfiguration
import gui.DirectorySelectionScreen
import utility.Logger

class CopyCatGui(config: CopyCatConfiguration) {
    init {
        application {
            val windowState = rememberWindowState(width = 900.dp, height = 700.dp)
            Window(onCloseRequest = ::exitApplication, title = "CopyCat", state = windowState) {
                DirectorySelectionScreen(initialConfig = config) { sourceDirs, compareDirs, destDirs ->
                    Logger.info("Next clicked - source=$sourceDirs, compare=$compareDirs, destination=$destDirs")
                    // TODO: navigate to the next screen once it exists
                }
            }
        }
    }
}
