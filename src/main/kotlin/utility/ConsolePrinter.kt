package utility

class ConsolePrinter {

    object AnsiColor {
        const val RESET = "\u001B[0m"
        const val RED = "\u001B[31m"
        const val GREEN = "\u001B[32m"
        const val YELLOW = "\u001B[33m"
        const val BLUE = "\u001B[34m"
        const val PURPLE = "\u001B[35m"
        const val CYAN = "\u001B[36m"
        const val WHITE = "\u001B[37m"
    }

    companion object {
        fun printColor(msg: String, color: String) {
            println("$color$msg${AnsiColor.RESET}")
        }

        fun printRed(msg: String) {
            printColor(msg, AnsiColor.RED)
        }

        fun printGreen(msg: String) {
            printColor(msg, AnsiColor.GREEN)
        }

        fun printYellow(msg: String) {
            printColor(msg, AnsiColor.YELLOW)
        }

        fun printBlue(msg: String) {
            printColor(msg, AnsiColor.BLUE)
        }

        fun printPurple(msg: String) {
            printColor(msg, AnsiColor.PURPLE)
        }

        fun printCyan(msg: String) {
            printColor(msg, AnsiColor.CYAN)
        }

        fun printWhite(msg: String) {
            printColor(msg, AnsiColor.WHITE)
        }
    }
}