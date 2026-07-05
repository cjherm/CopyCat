package utility

import java.io.File

object AppLocation {
    // Directory containing the running jar, or the classes dir when run from Gradle/IDE
    fun directory(): File {
        val codeSourceLocation = File(AppLocation::class.java.protectionDomain.codeSource.location.toURI())
        return if (codeSourceLocation.isFile) codeSourceLocation.parentFile else codeSourceLocation
    }
}
