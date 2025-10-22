import app.CopyCatApplication
import app.CopyCatConfiguration
import exceptions.UserWantsToQuitProgramException



fun main() {

    val config = CopyCatConfiguration()
    val copyCat = CopyCatApplication()

    try {
        copyCat.showWelcome()
        copyCat.getSrcAndTargetDirectoriesFromUser(config)
        copyCat.createUniqueFilesLists(config)
        copyCat.letUserSelectFileTypesToBeCopied(config)
        copyCat.letUserSelectTempDirectory(config)
        copyCat.copyFilesWithProgress(config)
    } catch (e: UserWantsToQuitProgramException) {
        println("User is quitting the program...")
        return
    }
}