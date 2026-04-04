package utility

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Logger {

    private const val MAX_BUFFER_SIZE = 1000
    private val timestampFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss")

    var printToConsole = false

    var logFile: File? = null
        set(value) {
            field = value
            flushLogBuffer()
        }

    private val logBuffer = mutableListOf<String>()

    fun info(msg: String) = log(msg, "[INFO]") { ConsolePrinter.printWhite(it) }
    fun warn(msg: String) = log(msg, "[WARN]") { ConsolePrinter.printYellow(it) }
    fun error(msg: String) = log(msg, "[ERROR]") { ConsolePrinter.printRed(it) }

    private fun log(msg: String, prefix: String, consolePrint: (String) -> Unit) {
        val timestamp = LocalDateTime.now().format(timestampFormatter)
        val formatted = "$timestamp $prefix $msg"
        if (printToConsole) consolePrint(formatted)
        if (logFile != null) {
            logInFile(formatted)
            return
        }
        if (logBuffer.size >= MAX_BUFFER_SIZE) {
            logBuffer.removeFirst()
            logBuffer.add("$timestamp [WARN] Log buffer was full, oldest message was dropped.")
        }
        logBuffer.add(formatted)
    }

    private fun flushLogBuffer() {
        while (logBuffer.isNotEmpty()) {
            logInFile(logBuffer.removeFirst())
        }
    }

    private fun logInFile(msg: String) {
        logFile?.appendText("$msg\n")
    }
}