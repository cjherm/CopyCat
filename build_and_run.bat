@echo off
echo Building the project and creating a launcher...
call gradlew build
call gradlew installDist
set "LAUNCHER_PATH=build\install\CopyCat\bin\CopyCat.bat"
echo Build succeeded.
echo Launcher created at: %LAUNCHER_PATH%
echo Running the launcher...
call %LAUNCHER_PATH%
pause