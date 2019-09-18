package org.brianbrown.search

import org.gradle.cli.CommandLineParser
import org.gradle.cli.ParsedCommandLine
import java.io.BufferedWriter
import java.io.File

fun main(args: Array<String>) {
    val fileSearch = FileSearch()
    val parsedCommandLine = CommandLineParser().apply {
        option("d").hasArgument(File::class.java)
    }.parse(*args)

    val directory = parsedCommandLine.directoryArgument()
    val searchPattern = parsedCommandLine.regexArgument()?.let { Regex(it) }

    System.out.bufferedWriter().use { writer ->
        writeOpeningStatement(directory, searchPattern, writer)
        fileSearch.search(directory, searchPattern, writer)
    }
}

fun ParsedCommandLine.directoryArgument(): File = when (hasOption("d")) {
    true -> option("d").value
    false -> "/"
}.let {
    File(it)
}.let {
    require(it.isDirectory) { "${it.name} is not a directory." }
    it
}

fun ParsedCommandLine.regexArgument(): String? = when (extraArguments.isNotEmpty()) {
    true -> {
        extraArguments.asSequence()
            .joinToString("|", "(", ")")
    }
    false -> null
}

fun writeOpeningStatement(
    directory: File,
    regex: Regex?,
    writer: BufferedWriter
) {
    when (regex) {
        null -> "Listing items beneath ${directory.absolutePath}"
        else -> "Searching beneath ${directory.absolutePath} for items matching $regex"
    }.also { writer.write(it) }
    writer.newLine()
}


