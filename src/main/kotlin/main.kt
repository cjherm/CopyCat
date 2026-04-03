import app.CopyCatApplication
import app.CopyCatConfiguration
import app.CopyCatConfigurationBuilder
import exceptions.UserWantsToQuitProgramException


fun main() {

    val config: CopyCatConfiguration
    val copyCat = CopyCatApplication()

    try {
        val cfgBuilder = CopyCatConfigurationBuilder()
        copyCat.showWelcome()
        copyCat.getSrcAndTargetDirectoriesFromUser(cfgBuilder)
        copyCat.createUniqueFilesLists(cfgBuilder)
        copyCat.letUserSelectFileTypesToBeCopied(cfgBuilder)
        copyCat.letUserSelectTempDirectory(cfgBuilder)
        config = cfgBuilder.build()
        copyCat.copyFilesWithProgress(config)
    } catch (_: UserWantsToQuitProgramException) {
        println("User is quitting the program...")
        return
    }
}