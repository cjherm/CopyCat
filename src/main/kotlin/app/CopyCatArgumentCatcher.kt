package app

import arguments.Argument
import arguments.ArgumentKey
import arguments.MultiValueArgument
import arguments.NoValueArgument
import arguments.SingleValueArgument

class CopyCatArgumentCatcher(private val args: Array<String>) {

    private val argsList: List<Argument> = parseArgs()

    fun requestsGui(): Boolean {
        val result = argsList.contains(NoValueArgument(ArgumentKey.GUI.key))
        println("requestsGui = $result")
        println(argsList)
        return result
    }

    fun getConfig(): CopyCatConfiguration? {
        // TODO
        return null
    }

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
