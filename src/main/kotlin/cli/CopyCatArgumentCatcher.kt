package cli

import cli.arguments.*
import config.CopyCatConfiguration
import utility.ConsolePrinter.Companion.printWhite
import utility.FileHelper
import utility.Logger
import java.io.File

class CopyCatArgumentCatcher(private val args: Array<String>) {

    private val argsList: List<Argument> = parseArgs()

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

    private fun retrieveLogToConsoleFromArg(): Boolean {
        val suppressLogToConsole = argsList.contains(Flag(ArgumentKey.NO_LOGC.key))
        Logger.printToConsole = !suppressLogToConsole
        if (suppressLogToConsole) {
            Logger.info("Logging to console disabled")
        }
        return !suppressLogToConsole
    }

    private fun retrieveLogFileFromArg(destDirs: List<File>): Boolean {
        val logArg = argsList.filterIsInstance<SingleValueArgument>().find { it.key == ArgumentKey.LOGF.key }
            ?: return false
        val logFilePath = logArg.value

        val logFile = when {
            logFilePath.isBlank() -> {
                val destDir = destDirs.firstOrNull()
                if (destDir == null) {
                    Logger.error("Cannot create default log file: no destination directory available.")
                    return false
                }
                File(destDir, Logger.defaultLogFileName())
            }

            File(logFilePath).isDirectory -> File(logFilePath, Logger.defaultLogFileName())

            else -> File(logFilePath)
        }

        if (logFile.isDirectory) {
            Logger.error("Log file path points to a directory, not a file: ${logFile.absolutePath}")
            return false
        }

        if (!logFile.exists()) {
            Logger.info("Log file does not exist and must be created: ${logFile.absolutePath}")
            logFile.parentFile?.mkdirs()
            if (!logFile.createNewFile()) {
                Logger.error("Log file could not be created: ${logFile.absolutePath}")
                return false
            }
        }

        Logger.logFile = logFile
        Logger.info("Set log file: ${logFile.absolutePath}")
        return true
    }

    fun getConfig(): CopyCatConfiguration {
        val printToConsole = retrieveLogToConsoleFromArg()
        val srcDirs = retrieveSrcDirsFromArg()
        val destDirs = retrieveDestDirsFromArg(srcDirs)
        val printToFile = retrieveLogFileFromArg(destDirs)
        val compDirsTemp = retrieveCompDirsFromArg(srcDirs)
        val inclTypes = retrieveInclTypesFromArg()
        val exclTypes = retrieveExclTypesFromArg()
        var configIsValid = true

        if (srcDirs.isEmpty() || destDirs.isEmpty()) {
            configIsValid = false
        }

        // no separate directory/ies set for comparison, so will compare to destination directory/ies
        val compDirs = compDirsTemp?: destDirs

        if(compDirs.isEmpty()){
            // the argument "-comp" was used at least once without a valid path, so we have a problem
            configIsValid = false
        }

        val filesSelectedToBeCopied = FileHelper.calculateFilesToCopy(srcDirs, compDirs, inclTypes, exclTypes)

        return CopyCatConfiguration(
            sourceDirs = srcDirs,
            copyDestDirs = destDirs,
            filesSelectedToBeCopied = filesSelectedToBeCopied,
            printToConsole = printToConsole,
            printToFile = printToFile,
            configIsValid = configIsValid
        )
    }

    private fun File.isValidDirectory() = exists() && isDirectory

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
                        ArgumentKey.SRC, ArgumentKey.DEST, ArgumentKey.COMP,
                        ArgumentKey.TYPES_INCL, ArgumentKey.TYPES_EXCL ->
                            SingleValueArgument(argumentKey.key, queue.removeFirstOrNull() ?: "")

                        // "-logf" may be followed by a path, or stand alone (next token is another flag or absent)
                        ArgumentKey.LOGF -> {
                            val value = queue.firstOrNull()?.takeIf { !it.startsWith("-") }?.also { queue.removeFirst() }
                            SingleValueArgument(argumentKey.key, value ?: "")
                        }

                        ArgumentKey.GUI, ArgumentKey.NO_LOGC ->
                            Flag(argumentKey.key)

                        ArgumentKey.HELP -> null
                    }

                listElem?.let { add(it) }
            }
        }
    }

    companion object {

        fun userRequestsHelp(args: Array<String>): Boolean {
            return args.contains("-${ArgumentKey.HELP.key}")
        }

        fun userRequestsGui(args: Array<String>): Boolean {
            return args.contains("-${ArgumentKey.GUI.key}")
        }

        fun printHelp() {
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
                        "\t-no-logc      Disable log to console (enabled by default)\n" +
                        "\t-logf         Enable log to file, written to the first destination directory as yyyy_MM_dd__HH_mm_ss.log\n" +
                        "\t-logf PATH    Enable log to file; PATH to a directory creates yyyy_MM_dd__HH_mm_ss.log there, PATH to a file writes/creates that exact file"
            )
        }
    }
}