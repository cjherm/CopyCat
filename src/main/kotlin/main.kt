import app.CopyCatApplication
import app.CopyCatShell
import app.CopyCatConfigurationBuilder
import exceptions.UserWantsToQuitProgramException


fun main(args: Array<String>) {
    startShell()
}

private fun startShell() {
    val ccShell = CopyCatShell()
    val ccApp = CopyCatApplication()
    try {
        val cfgBuilder = CopyCatConfigurationBuilder()
        ccShell.showWelcome()
        ccShell.getSrcAndTargetDirectoriesFromUser(cfgBuilder)
        ccShell.createUniqueFilesLists(cfgBuilder)
        ccShell.letUserSelectFileTypesToBeCopied(cfgBuilder)
        ccShell.letUserSelectTempDirectory(cfgBuilder)
        val config = cfgBuilder.build()
        ccApp.launch(config)
    } catch (_: UserWantsToQuitProgramException) {
        println("User is quitting the program...")
        return
    }
}