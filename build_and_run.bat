:: Suppress echoing each command to the console
@echo off

:: Run the Gradle 'jar' task, which compiles the code and packages it
:: along with all runtime dependencies into a single fat JAR file
echo Building the project...
call gradlew jar

:: If Gradle exited with an error code, abort here instead of trying to run a broken build
if errorlevel 1 (
    echo Build failed.
    pause
    exit /b 1
)

:: Copy the built JAR from the Gradle output folder to the project root,
:: overwriting any previously built version (/Y suppresses the overwrite prompt)
echo Build succeeded.
copy /Y "build\libs\CopyCat-1.0-SNAPSHOT.jar" "CopyCat.jar"
echo JAR copied to project root as CopyCat.jar

:: Launch the JAR using the system Java runtime
echo Running CopyCat.jar...
java -jar CopyCat.jar

:: Keep the console window open after the program exits so you can read any output
pause