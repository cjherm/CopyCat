package cli

import cli.arguments.*
import config.CopyCatConfiguration
import utility.FileHelper
import utility.Logger
import java.io.File

class CopyCatArgumentCatcher(private val args: Array<String>) {

    private val argsList: List<Argument> = parseArgs()

    fun requestsGui(): Boolean {
        val requestedGui = argsList.contains(NoValueArgument(ArgumentKey.GUI.key))
        if (requestedGui) {
            Logger.info("Requested GUI")
        }
        return requestedGui
    }

    fun retrieveSrcDirFromArg(): File? {
        val argValue = argsList.singleValueOf(ArgumentKey.SRC) ?: ""
        if (argValue.isEmpty()) {
            Logger.error("Missing path for source directory! Please add via \"-src PATH\"!")
            return null
        }

        val srcDir = File(argValue)
        if (!srcDir.isValidDirectory()) {
            Logger.error("Source directory is invalid or does not exist: ${srcDir.absolutePath}")
            return null
        }

        srcDir.listFiles()?.size?.let {
            if (it <= 0) {
                Logger.error("Source directory is empty!")
                return null
            }
        }

        Logger.info("Set source directory: ${srcDir.absolutePath}")
        return srcDir
    }

    fun retrieveDestDirFromArg(srcDir: File?): File? {
        val argValue = argsList.singleValueOf(ArgumentKey.DEST) ?: ""
        if (argValue.isEmpty()) {
            Logger.error("Missing path for source directory! Please add via \"-dest PATH\"!")
            return null
        }

        val destDir = File(argValue)
        if (!destDir.isValidDirectory()) {
            Logger.warn("Destination directory is invalid or does not exist and must be created: ${destDir.absolutePath}")
            return null
        }

        if (destDir == srcDir) {
            Logger.error("Destination directory cannot be the same as the source directory!")
            return null
        }

        if (srcDir != null && !destDir.isValidDirectory()) {
            destDir.mkdirs()
            if (!destDir.isValidDirectory()) {
                Logger.error("Destination directory could not be created: ${destDir.absolutePath}")
                return null
            }
            Logger.info("Destination directory created.")
        }

        Logger.info("Set destination directory: ${destDir.absolutePath}")
        return destDir
    }

    fun retrieveCompDirFromArg(srcDir: File?): File? {
        val argValue = argsList.singleValueOf(ArgumentKey.COMP) ?: ""
        if (argValue.isEmpty()) {
            return null
        }

        val compDir = File(argValue)
        if (!compDir.isValidDirectory()) {
            Logger.error("Diff directory is invalid or does not exist: ${compDir.absolutePath}")
            return null
        }
        if (compDir == srcDir) {
            Logger.error("Diff directory cannot be the same as the source directory!")
            return null
        }

        Logger.info("Set diff directory: ${compDir.absolutePath}")
        return compDir
    }

    private fun retrieveTypesFromArg(): List<String> {
        val types = argsList.multiValuesOf(ArgumentKey.TYPES)
        if (types.isEmpty()) {
            Logger.info("No file type filter set, will consider all files in source directory!")
        } else {
            Logger.info("Types selected: ${types.toList()}")
        }
        return types
    }

    private fun retrieveLogSettingsFromArg() {
        val logFilePath = argsList.singleValueOf(ArgumentKey.LOGF)
        if (logFilePath != null) Logger.logFile = File(logFilePath)
        if (argsList.contains(NoValueArgument(ArgumentKey.LOGC.key))) Logger.printToConsole = true
    }

    fun getConfig(): CopyCatConfiguration? {
        retrieveLogSettingsFromArg()
        val srcDir = retrieveSrcDirFromArg()
        val compDirTemp = retrieveCompDirFromArg(srcDir)
        val destDir = retrieveDestDirFromArg(srcDir)
        val types = retrieveTypesFromArg()

        if (srcDir == null || destDir == null) {
            return null
        }

        val compDir = compDirTemp ?: destDir

        // TODO 1 this does not seem to work properly, it still seems to include files from different types then requested
        // TODO 3 make sure we only include unique files
        val filesSelectedToBeCopied = calculateFilesToCopy(srcDir, compDir, types)

        return CopyCatConfiguration(
            sourceDir = srcDir,
            copyDestDir = destDir,
            filesSelectedToBeCopied = filesSelectedToBeCopied
        )
    }

    private fun calculateFilesToCopy(srcDir: File, compDir: File, types: List<String>): List<File> {
        val uniqueFiles = FileHelper.findMissingFilesGroupedByType(srcDir, compDir)
        return if (types.isEmpty()) {
            uniqueFiles.values.flatten()
        } else {
            types.mapNotNull { uniqueFiles[it] }.flatten()
        }
    }

    private fun File.isValidDirectory() = exists() && isDirectory

    private fun List<Argument>.singleValueOf(key: ArgumentKey): String? =
        filterIsInstance<SingleValueArgument>().find { it.key == key.key }?.value

    private fun List<Argument>.multiValuesOf(key: ArgumentKey): List<String> =
        filterIsInstance<MultiValueArgument>().find { it.key == key.key }?.values ?: emptyList()

    private fun parseArgs(): List<Argument> {
        val queue = ArrayDeque(args.toList())
        return buildList {
            while (queue.isNotEmpty()) {
                val arg = queue.removeFirst()
                if (!arg.startsWith("-")) continue
                val argumentKey = ArgumentKey.fromString(arg.removePrefix("-")) ?: continue
                add(
                    when (argumentKey) {
                        ArgumentKey.SRC, ArgumentKey.DEST, ArgumentKey.COMP, ArgumentKey.LOGF ->
                            SingleValueArgument(argumentKey.key, queue.removeFirstOrNull() ?: "")

                        ArgumentKey.TYPES ->
                            MultiValueArgument(argumentKey.key, drainWhile(queue) { !it.startsWith("-") })

                        ArgumentKey.GUI, ArgumentKey.LOGC ->
                            NoValueArgument(argumentKey.key)
                    }
                )
            }
        }
    }

    private fun drainWhile(queue: ArrayDeque<String>, predicate: (String) -> Boolean): List<String> =
        buildList {
            while (queue.isNotEmpty() && predicate(queue.first())) {
                add(queue.removeFirst())
            }
        }
}
