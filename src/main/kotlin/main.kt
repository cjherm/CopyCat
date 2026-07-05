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
        showHelp()
        return
    }

    val caughtArgs = CopyCatArgumentCatcher(args)
    val config = caughtArgs.getConfig()

    if (CopyCatArgumentCatcher.userRequestsGui(args)) {
        startGui(config)
        return
    }

    if (config != null) {
        startImmediateExecution(config)
        return
    }
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

private fun showHelp() {
    printWhite(
        "\nCopyCat offers you the possibility to copy files from at least one directory to one or multiple destination directory/-ies. It will check for possible duplicates before copying anything by comparing the contents. You can also separate directories for comparison and destination. Additionally a specific set of file types can be selected or excluded.\n" +
                "\n" +
                "REQUIRED:\n" +
                "\t-src PATH     Directory whose contents are to be copied\n" +
                "\t-dest PATH    Destination directory\n" +
                "-----------------------------------------------------------------\n" +
                "OPTIONAL:\n" +
                "\t-src PATH     For every other source directory\n" +
                "\t-dest PATH    For every other destination directory\n" +
                "\t-comp PATH    When you want to compare to different directory/ies than the one/s for destination\n" +
                "\t-incl TYPE    Only include this file type\n" +
                "\t-excl TYPE    Exclude this file types\n" +
                "\t-gui          If you want to use the GUI\n" +
                "\t-logc         Enable log to console\n" +
                "\t-logf         Enable log to file, written to the first destination directory as yyyy_MM_dd__HH_mm_ss.log\n" +
                "\t-logf PATH    Enable log to file; PATH to a directory creates yyyy_MM_dd__HH_mm_ss.log there, PATH to a file writes/creates that exact file"
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