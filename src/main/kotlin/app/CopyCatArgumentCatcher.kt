package app

import arguments.Argument
import arguments.MultiValueArgument
import arguments.NoValueArgument
import arguments.SingleValueArgument

class CopyCatArgumentCatcher(private val args: Array<String>) {

    private val argsList: List<Argument> = parseArgs()

    fun requestsGui() = argsList.contains(NoValueArgument(Argument.GUI))

    fun hasSufficientArgs(): Boolean = false

    fun getConfig(): CopyCatConfiguration = CopyCatConfiguration(
        sourceDir = java.io.File(""),
        copyDestDir = java.io.File(""),
        filesSelectedToBeCopied = emptyList()
    )

    private fun parseArgs(): List<Argument> {
        val queue = ArrayDeque(args.toList())
        return buildList {
            while (queue.isNotEmpty()) {
                val arg = queue.removeFirst()
                if (!arg.startsWith("-")) continue
                val key = arg.removePrefix("-")
                add(
                    when (key) {
                        Argument.SRC, Argument.DEST, Argument.COMP, Argument.LOG ->
                            SingleValueArgument(key, queue.removeFirstOrNull() ?: "")

                        Argument.TYPES ->
                            MultiValueArgument(key, drainWhile { !it.startsWith("-") })

                        else ->
                            NoValueArgument(key)
                    }
                )
            }
        }
    }

    private fun drainWhile(predicate: (String) -> Boolean): List<String> =
        buildList {
            while (isNotEmpty() && predicate(first())) {
                add(removeFirst())
            }
        }
}
