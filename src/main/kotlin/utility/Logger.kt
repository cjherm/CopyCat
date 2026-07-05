package utility

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object Logger {

    private val TIME_STAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss")
    private const val MAX_BUFFER_SIZE = 1000
    private const val TERMINATION_TIMEOUT = 5L
    private const val LOGGER_THREAD_NAME = "logger-io"

    init {
        // Give the background thread a chance to drain pending log entries before the JVM exits.
        Runtime.getRuntime().addShutdownHook(Thread {
            ioExecutor.shutdown()
            try {
                ioExecutor.awaitTermination(TERMINATION_TIMEOUT, TimeUnit.SECONDS)
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        })
    }

    private val ioExecutor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, LOGGER_THREAD_NAME).apply { isDaemon = true }
    }

    @Volatile
    var printToConsole = true

    @Volatile
    var logFile: File? = null
        set(value) {
            field = value
            ioExecutor.execute {
                if (value != null) {
                    process(
                        format(
                            "New session started - CopyCat v$version",
                            "[INFO] "
                        )
                    ) { ConsolePrinter.printWhite(it) }
                    flushLogBuffer()
                }
            }
        }

    private val logBuffer = ArrayDeque<String>()

    private val version: String
        get() = Logger::class.java.`package`?.implementationVersion ?: "unknown"

    fun info(msg: String) = log(msg, "[INFO] ") { ConsolePrinter.printWhite(it) }
    fun warn(msg: String) = log(msg, "[WARN] ") { ConsolePrinter.printYellow(it) }
    fun error(msg: String) = log(msg, "[ERROR]") { ConsolePrinter.printRed(it) }

    private fun log(msg: String, prefix: String, consolePrint: (String) -> Unit) {
        val formatted = format(msg, prefix)
        ioExecutor.execute { process(formatted, consolePrint) }
    }

    private fun format(msg: String, prefix: String): String {
        val timestamp = LocalDateTime.now().format(TIME_STAMP_FORMATTER)
        return "$timestamp $prefix $msg"
    }

    private fun process(formatted: String, consolePrint: (String) -> Unit) {
        if (printToConsole) consolePrint(formatted)

        if (logFile != null) {
            logInFile(formatted)
            return
        }

        if (logBuffer.size >= MAX_BUFFER_SIZE) {
            logBuffer.removeFirst()
            logBuffer.addLast(format("Log buffer was full, oldest message was dropped.", "[WARN] "))
        }
        logBuffer.addLast(formatted)
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