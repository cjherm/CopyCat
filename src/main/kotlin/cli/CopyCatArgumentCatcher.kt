package cli

import cli.arguments.*
import config.CopyCatConfiguration
import utility.FileHelper
import utility.Logger
import java.io.File

class CopyCatArgumentCatcher(private val args: Array<String>) {

    private val argsList: List<Argument> = parseArgs()

    fun requestsGui(): Boolean {
        val requestedGui = argsList.contains(Flag(ArgumentKey.GUI.key))
        if (requestedGui) {
            Logger.info("Requested GUI")
        }
        return requestedGui
    }

    fun retrieveSrcDirsFromArg(): List<File> {
        val argValues = argsList.allValuesOf(ArgumentKey.SRC)
        if (argValues.isEmpty()) return emptyList()

        return argValues.mapNotNull { argValue ->
            val srcDir = File(argValue)
            when {
                !srcDir.isValidDirectory() -> {
                    Logger.error("Source directory is invalid or does not exist: ${srcDir.absolutePath}")
                    null
                }

                (srcDir.listFiles()?.size ?: 0) <= 0 -> {
                    Logger.error("Source directory is empty: ${srcDir.absolutePath}")
                    null
                }

                else -> {
                    Logger.info("Set source directory: ${srcDir.absolutePath}")
                    srcDir
                }
            }
        }
    }

    fun retrieveDestDirsFromArg(srcDirs: List<File>): List<File> {
        val argValues = argsList.allValuesOf(ArgumentKey.DEST)
        if (argValues.isEmpty()) return emptyList()

        return argValues.mapNotNull { argValue ->
            val destDir = File(argValue)
            when {
                srcDirs.contains(destDir) -> {
                    Logger.error("Destination directory cannot be the same as a source directory: ${destDir.absolutePath}")
                    null
                }

                !destDir.isValidDirectory() -> {
                    Logger.info("Destination directory does not exist and must be created: ${destDir.absolutePath}")
                    destDir.mkdirs()
                    if (!destDir.isValidDirectory()) {
                        Logger.error("Destination directory could not be created: ${destDir.absolutePath}")
                        null
                    } else {
                        Logger.info("Destination directory created: ${destDir.absolutePath}")
                        destDir
                    }
                }

                else -> {
                    Logger.info("Set destination directory: ${destDir.absolutePath}")
                    destDir
                }
            }
        }
    }

    fun retrieveCompDirsFromArg(srcDirs: List<File>): List<File>? {
        val argValues = argsList.allValuesOf(ArgumentKey.COMP)
        if (argValues.isEmpty()) return null

        return argValues.mapNotNull { argValue ->
            val compDir = File(argValue)
            when {
                !compDir.isValidDirectory() -> {
                    Logger.error("Diff directory is invalid or does not exist: ${compDir.absolutePath}")
                    null
                }

                srcDirs.contains(compDir) -> {
                    Logger.error("Diff directory cannot be the same as a source directory: ${compDir.absolutePath}")
                    null
                }

                else -> {
                    Logger.info("Set diff directory: ${compDir.absolutePath}")
                    compDir
                }
            }
        }
    }

    private fun retrieveInclTypesFromArg(): List<String> {
        val types = argsList.allValuesOf(ArgumentKey.TYPES_INCL)
        if (types.isEmpty()) {
            Logger.info("No include type filter set, will consider all files in source directory!")
        } else {
            Logger.info("Include types selected: $types")
        }
        return types
    }

    private fun retrieveExclTypesFromArg(): List<String> {
        val types = argsList.allValuesOf(ArgumentKey.TYPES_EXCL)
        if (types.isEmpty()) {
            Logger.info("No exclude type filter set.")
        } else {
            Logger.info("Exclude types selected: $types")
        }
        return types
    }

    private fun retrieveLogSettingsFromArg() {
        val logFilePath = argsList.singleValueOf(ArgumentKey.LOGF)
        if (logFilePath != null) Logger.logFile = File(logFilePath)
        if (argsList.contains(Flag(ArgumentKey.LOGC.key))) Logger.printToConsole = true
    }

    fun getConfig(): CopyCatConfiguration? {
        retrieveLogSettingsFromArg()
        val srcDirs = retrieveSrcDirsFromArg()
        val destDirs = retrieveDestDirsFromArg(srcDirs)
        val compDirsTemp = retrieveCompDirsFromArg(srcDirs)
        val inclTypes = retrieveInclTypesFromArg()
        val exclTypes = retrieveExclTypesFromArg()

        if (srcDirs.isEmpty() || destDirs.isEmpty()) {
            return null
        }

        // no separate directory/ies set for comparison, so will compare to destination directory/ies
        val compDirs = compDirsTemp?: destDirs

        if(compDirs.isEmpty()){
            // the argument "-comp" was used at least once without a valid path, so we have a problem
            return null
        }

        // TODO 1 this does not seem to work properly, it still seems to include files from different types then requested
        // TODO 3 make sure we only include unique files
        // TODO what do we do when the destination dir/s already have a file as we do not compare them when using -comp
        val filesSelectedToBeCopied = calculateFilesToCopy(srcDirs, compDirs, inclTypes, exclTypes)

        return CopyCatConfiguration(
            sourceDirs = srcDirs,
            copyDestDirs = destDirs,
            filesSelectedToBeCopied = filesSelectedToBeCopied,
            printToConsole = false,
            printToFile = false
        )
    }
    // TODO Fix this temporary pseudo fix
    private fun calculateFilesToCopy(srcDir: List<File>, compDir: List<File>, types: List<String>, types2: List<String>): List<File> {
        // TODO Fix this temporary pseudo fix
        val uniqueFiles = FileHelper.findMissingFilesGroupedByType(srcDir[0], compDir[1])
        return if (types.isEmpty()) {
            uniqueFiles.values.flatten()
        } else {
            types.mapNotNull { uniqueFiles[it] }.flatten()
        }
    }

    private fun File.isValidDirectory() = exists() && isDirectory

    private fun List<Argument>.singleValueOf(key: ArgumentKey): String? =
        filterIsInstance<SingleValueArgument>().find { it.key == key.key }?.value

    private fun List<Argument>.allValuesOf(key: ArgumentKey): List<String> =
        filterIsInstance<SingleValueArgument>().filter { it.key == key.key }.map { it.value }

    private fun parseArgs(): List<Argument> {
        val queue = ArrayDeque(args.toList())
        return buildList {
            while (queue.isNotEmpty()) {
                val arg = queue.removeFirst()
                if (!arg.startsWith("-")) continue
                val argumentKey = ArgumentKey.fromString(arg.removePrefix("-")) ?: continue
                val listElem =
                    when (argumentKey) {
                        ArgumentKey.SRC, ArgumentKey.DEST, ArgumentKey.COMP, ArgumentKey.LOGF,
                        ArgumentKey.TYPES_INCL, ArgumentKey.TYPES_EXCL ->
                            SingleValueArgument(argumentKey.key, queue.removeFirstOrNull() ?: "")

                        ArgumentKey.GUI, ArgumentKey.LOGC ->
                            Flag(argumentKey.key)

                        ArgumentKey.HELP -> null
                    }

                listElem?.let { add(it) }
            }
        }
    }

    companion object {
        fun containsHelpArg(args: Array<String>): Boolean {
            return args.contains("-${ArgumentKey.HELP.key}")
        }
    }
}