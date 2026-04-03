package app

class CopyCatArgumentCatcher(args: Array<String>) {

    fun requestsGui(): Boolean {
        return false
    }

    fun hasSufficientArgs(): Boolean {
        return false
    }

    fun getConfig(): CopyCatConfiguration {
        return CopyCatConfiguration(
            sourceDir = java.io.File(""),
            copyDestDir = java.io.File(""),
            filesSelectedToBeCopied = emptyList()
        )
    }
}