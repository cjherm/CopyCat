import app.CopyCatApplication
import app.CopyCatArgumentCatcher
import app.CopyCatConfiguration
import app.CopyCatShell
import app.CopyCatConfigurationBuilder
import exceptions.UserWantsToQuitProgramException


fun main(args: Array<String>) {

    val argCatcher = CopyCatArgumentCatcher(args)

    if (argCatcher.requestsGui()) {
        // Like CopyCatShell but with a GUI
        startGui()
        return
    }

    if (argCatcher.hasSufficientArgs()) {
        // When all arguments are provided and no GUI requests, we will launch CopyCat directly without a Shell nor GUI
        startImmediateExecution(argCatcher.getConfig())
        return
    }

    main()
}

fun main() {
    // When no or not all arguments are provided and no GUI requests, we will launch CopyCat for Shell
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

private fun startGui() {
    // TODO
}

private fun startImmediateExecution(config: CopyCatConfiguration) {
    val ccApp = CopyCatApplication()
    ccApp.launch(config)
}