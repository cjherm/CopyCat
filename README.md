# CopyCat

<mark>STILL WORK IN PROGRESS!</mark> CopyCat is a command-line tool that copies files from one directory to another while skipping duplicates. It compares a source directory against a reference directory, identifies files that are missing from the reference, and copies only those. You can filter by file type so only the formats you care about are transferred.

It was built with situations like offloading a camera SD card or an external drive in mind, where you want to pull in new files without duplicating what you already have.

## Requirements

Java 17 or higher must be installed on your machine.

## Running CopyCat

The simplest way is to double-click `run.bat`, which launches the pre-built JAR directly. Alternatively, run it from a terminal:

```
java -jar CopyCat.jar
```

If no arguments are provided, CopyCat starts in interactive shell mode and guides you through the process step by step.

## Command-line arguments

You can skip the interactive prompts by passing arguments directly:

| Argument | Description |
|---|---|
| `-src <path>` | Path to the source directory (the one you want to copy from) |
| `-dest <path>` | Path to the destination directory (where files will be copied to) |
| `-comp <path>` | Path to the directory to compare against for duplicates |
| `-types <ext...>` | One or more file extensions to include, e.g. `-types jpg png mp4` |
| `-log <path>` | Path to a log file |
| `-gui` | Launch with a graphical interface (not yet available) |

When all required arguments are provided, CopyCat runs immediately without any prompts.

## Building from source

Run `build_and_run.bat` to compile the project and produce a fresh `CopyCat.jar` in the project root. This requires Gradle, which is included via the Gradle wrapper so no separate installation is needed.

```
build_and_run.bat
```

The script builds the project, copies the JAR to the project root, and runs it straight away.

## TODO's left
* Rewrite this readme intro
* launching via cmd args does not work yet
** The shell procedure must be adapted to also make cmd args possible
* Create GUI
* Add more tests and adapt existing ones