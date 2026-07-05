import app.CopyCatApplication
import cli.CopyCatArgumentCatcher
import cli.CopyCatShell
import config.CopyCatConfiguration
import config.CopyCatConfigurationBuilder
import utility.ConsolePrinter.Companion.printWhite
import utility.ConsolePrinter.Companion.printYellow
import utility.RestartProgramException
import utility.UserWantsToQuitProgramException


fun main(args: Array<String>) {

    if (args.isEmpty()) {
        startShell()
        return
    }

    if (CopyCatArgumentCatcher.userRequestsHelp(args)) {
        CopyCatArgumentCatcher.printHelp()
        return
    }

    val caughtArgs = CopyCatArgumentCatcher(args)
    val config = caughtArgs.getConfig()

    if (CopyCatArgumentCatcher.userRequestsGui(args)) {
        startGui(config)
        return
    }

    startImmediateExecution(config)
}

private fun startImmediateExecution(config: CopyCatConfiguration) {
    val ccApp = CopyCatApplication()
    ccApp.launch(config)
}

private fun startGui(config: CopyCatConfiguration?) {
    printYellow("startGui() WORK IN PROGRESS")
    // TODO 5 implement GUI
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
        ccShell.letUserDecideToStart()
        val config = cfgBuilder.build()
        ccApp.launch(config)
    } catch (_: UserWantsToQuitProgramException) {
        printWhite("\nUser is quitting CopyCat...")
        return
    } catch (_: RestartProgramException) {
        startUserInteraction(ccShell)
    }
}