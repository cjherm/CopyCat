package utility

object Logger {

    fun info(msg: String) {
        ConsolePrinter.printWhite("[INFO]  $msg")
    }

    fun success(msg: String) {
        ConsolePrinter.printGreen("[OK]    $msg")
    }

    fun warn(msg: String) {
        ConsolePrinter.printYellow("[WARN]  $msg")
    }

    fun error(msg: String) {
        ConsolePrinter.printRed("[ERROR] $msg")
    }
}