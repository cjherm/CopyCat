package app

import arguments.Argument
import arguments.ArgumentKey
import arguments.MultiValueArgument
import arguments.NoValueArgument
import arguments.SingleValueArgument
import utility.ConsolePrinter.Companion.printRed
import utility.FileHelper
import java.io.File

class CopyCatArgumentCatcher(private val args: Array<String>) {

    private val argsList: List<Argument> = parseArgs()

    fun requestsGui(): Boolean {
        return argsList.contains(NoValueArgument(ArgumentKey.GUI.key))
    }

    fun getConfig(): CopyCatConfiguration? {
        val srcDir  = File(argsList.singleValueOf(ArgumentKey.SRC)  ?: "")
        val compDir = File(argsList.singleValueOf(ArgumentKey.COMP) ?: "")
        val destDir = File(argsList.singleValueOf(ArgumentKey.DEST) ?: "")
        val types   = argsList.multiValuesOf(ArgumentKey.TYPES)

        if (!srcDir.isValidDirectory()) {
            printRed("Source directory is invalid or does not exist: ${srcDir.absolutePath}")
            return null
        }
        if (!compDir.isValidDirectory()) {
            printRed("Comparison directory is invalid or does not exist: ${compDir.absolutePath}")
            return null
        }
        if (!destDir.isValidDirectory()) {
            printRed("Destination directory is invalid or does not exist: ${destDir.absolutePath}")
            return null
        }
        if (srcDir == compDir) {
            printRed("Source and comparison directory must not be the same.")
            return null
        }

        val uniqueFiles = FileHelper.findMissingFilesGroupedByType(srcDir, compDir)
        val filesSelectedToBeCopied = if (types.isEmpty()) {
            uniqueFiles.values.flatten()
        } else {
            types.mapNotNull { uniqueFiles[it] }.flatten()
        }

        return CopyCatConfiguration(
            sourceDir = srcDir,
            copyDestDir = destDir,
            filesSelectedToBeCopied = filesSelectedToBeCopied
        )
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
                        ArgumentKey.SRC, ArgumentKey.DEST, ArgumentKey.COMP, ArgumentKey.LOG ->
                            SingleValueArgument(argumentKey.key, queue.removeFirstOrNull() ?: "")

                        ArgumentKey.TYPES ->
                            MultiValueArgument(argumentKey.key, drainWhile(queue) { !it.startsWith("-") })

                        ArgumentKey.GUI ->
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
