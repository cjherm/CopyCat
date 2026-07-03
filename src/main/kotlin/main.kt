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

    val caughtArgs = CopyCatArgumentCatcher(args)
    val config = caughtArgs.getConfig()

    if (caughtArgs.requestsHelp()) {
        showHelp()
        return
    }

    if (caughtArgs.requestsGui()) {
        startGui(config)
        return
    }

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

private fun showHelp() {
    printWhite(
        "CopyCat offers you the possibility to copy files from at least one directory to one or multiple destination directory/-ies. It will check for possible duplicates before copying anything by comparing the contents. You can also separate directories for comparison and destination. Additionally a specific set of file types can be selected or excluded.\n" +
                "\n" +
                "REQUIRED:\n" +
                "\t-src PATH   /  Directory whose contents are to be copied\n" +
                "\t-dest PATH  /  Destination directory" +
                "\n--------------------------------------------------------\n" +
                "OPTIONAL:\n" +
                "\t-src PATH   /  For every other source directory\n" +
                "\t-dest PATH  /  For every other destination directory" +
                "\t-comp PATH  /  When you want to compare to different directory/ies than the one/s for destination\n" +
                "\t-incl TYPE  /  Only include this file type" +
                "\t-excl TYPE  /  Exclude this file types" +
                "\t-gui        /  If you want to use the GUI\n" +
                "\t-logc       /  Enable log to console\n" +
                "\t-logf PATH  /  Filepath and name where the log should be written to"
    )
}

private fun startGui(config: CopyCatConfiguration?) {
    printYellow("startGui() WORK IN PROGRESS")
    // TODO 5 implement GUI
}

private fun startImmediateExecution(config: CopyCatConfiguration) {
    printYellow("startImmediateExecution() WORK IN PROGRESS")
    printWhite(config.toString())
    // TODO 6 implement immediate execution via command-line arguments
}