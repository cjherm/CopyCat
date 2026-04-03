import app.CopyCatApplication
import app.CopyCatArgumentCatcher
import app.CopyCatConfiguration
import app.CopyCatShell
import app.CopyCatConfigurationBuilder
import exceptions.UserWantsToQuitProgramException


fun main(args: Array<String>) {

    if (args.isEmpty()) {
        startShell()
        return
    }

    val argCatcher = CopyCatArgumentCatcher(args)

    if (argCatcher.requestsGui()) {
        // Like CopyCatShell but with a GUI
        startGui()
        return
    }

    val config = argCatcher.getConfig()
    if (config != null) {
        // When all arguments are provided and no GUI requests, we will launch CopyCat directly without a Shell nor GUI
        startImmediateExecution(config)
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
        println("User is quitting the program...")
        return
    }
}

private fun startGui() {
    println("startGui() WORK IN PROGRESS")
    // TODO
}

private fun startImmediateExecution(config: CopyCatConfiguration) {
    println("startImmediateExecution() WORK IN PROGRESS")
    println(config)
    // TODO
    //val ccApp = CopyCatApplication()
    //ccApp.launch(config)
}