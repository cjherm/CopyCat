import app.CopyCatApplication
import cli.CopyCatArgumentCatcher
import cli.CopyCatShell
import config.CopyCatConfiguration
import config.CopyCatConfigurationBuilder
import utility.ConsolePrinter.Companion.printRed
import utility.ConsolePrinter.Companion.printWhite
import utility.ConsolePrinter.Companion.printYellow
import utility.RestartProgramException
import utility.UserWantsToQuitProgramException


fun main(args: Array<String>) {

    if (args.isEmpty()) {
        startShell()
        return
    }

    // TODO Add option to use GUI and some preset config settings
    val caughtArgs = CopyCatArgumentCatcher(args)
    if (caughtArgs.requestsGui()) {
        startGui()
        return
    }

    val config = caughtArgs.getConfig()
    if (config != null) {
        startImmediateExecution(config)
        return
    }

    printRed("Missing or invalid arguments for immediate execution! Please add at least \"-src PATH -dest PATH\" as minimum!")
}

private fun startShell() {
    val ccShell = CopyCatShell()
    ccShell.showWelcome()
    startUserInteraction(ccShell)
}

private fun startUserInteraction(ccShell: CopyCatShell) {
    val ccApp = CopyCatApplication()
    try {
        val cfgBuilder = CopyCatConfigurationBuilder()
        cfgBuilder.printToConsole = true
        ccShell.getSrcAndTargetDirectoriesFromUser(cfgBuilder)
        ccShell.createUniqueFilesLists(cfgBuilder)
        ccShell.letUserSelectFileTypesToBeCopied(cfgBuilder)
        ccShell.letUserSelectTempDirectory(cfgBuilder)
        ccShell.letUserDecideOnLogFile(cfgBuilder)
        val config = cfgBuilder.build()
        ccApp.launch(config)
    } catch (_: UserWantsToQuitProgramException) {
        printWhite("User is quitting the program...")
        return
    } catch (_: RestartProgramException) {
        startUserInteraction(ccShell)
    }
}

private fun startGui() {
    printYellow("startGui() WORK IN PROGRESS")
    // TODO 5 implement GUI
}

private fun startImmediateExecution(config: CopyCatConfiguration) {
    printYellow("startImmediateExecution() WORK IN PROGRESS")
    printWhite(config.toString())
    // TODO 6 implement immediate execution via command-line arguments
}