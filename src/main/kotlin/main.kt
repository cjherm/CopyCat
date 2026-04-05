import app.CopyCatApplication
import cli.CopyCatArgumentCatcher
import cli.CopyCatShell
import config.CopyCatConfiguration
import config.CopyCatConfigurationBuilder
import utility.UserWantsToQuitProgramException
import utility.Logger


fun main(args: Array<String>) {

    if (args.isEmpty()) {
        startShell()
        return
    }

    val argCatcher = CopyCatArgumentCatcher(args)
    val config = argCatcher.getConfig()

    if (argCatcher.requestsGui()) {
        // Like CopyCatShell but with a GUI
        startGui()
        return
    }

    if (config != null) {
        // When all arguments are provided and no GUI requests, we will launch CopyCat directly without a Shell nor GUI
        startImmediateExecution(config)
    } else {
        Logger.error("Missing or invalid arguments for immediate execution! Please add at least \"-src PATH -dest PATH\" as minimum!")
    }
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
        Logger.info("User is quitting the program...")
        return
    }
}

private fun startGui() {
    Logger.warn("startGui() WORK IN PROGRESS")
    // TODO 5 implement GUI
}

private fun startImmediateExecution(config: CopyCatConfiguration) {
    Logger.warn("startImmediateExecution() WORK IN PROGRESS")
    Logger.info(config.toString())
    // TODO 6 implement immediate execution via command-line arguments
    //val ccApp = CopyCatApplication()
    //ccApp.launch(config)
}
