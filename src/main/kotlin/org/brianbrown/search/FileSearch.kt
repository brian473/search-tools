package org.brianbrown.search

import mu.KotlinLogging
import java.io.BufferedWriter
import java.io.File

open class FileSearch {
    val logger = KotlinLogging.logger(FileSearch::class.java.name)

    fun search(rootDir: String, pattern: String? = null, terms: String? = null) {
        val directory = getRootDirectory(rootDir)
        val patternRegex = pattern?.let { getPatternRegex(it) }
        val termsRegex = terms?.let { getSearchTermsRegex(it) }

        val directoryGraph = FileType.Directory(directory)

        System.out.bufferedWriter().use {
            writeOpeningStatement(it, directory, patternRegex, termsRegex)
            val filteredItems = getFilteredItems(directoryGraph, patternRegex, termsRegex)
            writeContents(it, filteredItems)
        }
    }

    private fun getSearchTermsRegex(terms: String): Regex? {
        return try {
            val termsList = terms.split(",")
                .map { it.trim() }
            when (termsList.isEmpty()) {
                true -> null
                false -> Regex(
                    termsList.joinToString(separator = "|", prefix = "(", postfix = ")")
                )
            }
        } catch (e: Exception) {
            logger.warn { "Exception when creating search terms regex, ignoring value." }
            null
        }
    }

    private fun logException(message: String, exception: Exception? = null) {
        logger.warn { message }
        exception?.let {
            when (logger.isTraceEnabled) {
                true -> logger.trace { exception.stackTrace?.contentDeepToString() }
                false -> logger.debug { "Error message: ${exception.localizedMessage}" }
            }
        }
    }

    private fun getPatternRegex(pattern: String): Regex? {
        return try {
            Regex.fromLiteral(pattern)
        } catch (e: Exception) {
            logException("Error parsing regex, pattern will not be applied.", e)
            null
        }
    }

    private fun getRootDirectory(rootDir: String) = File(rootDir).let {
        when (it.isDirectory) {
            true -> it
            false -> {
                logException("$rootDir is not a directory, using working directory instead.")
                File(".")
            }
        }
    }

    private fun writeOpeningStatement(
        writer: BufferedWriter,
        directory: File,
        patternRegex: Regex? = null,
        termsRegex: Regex? = null
    ) {
        val optionsSegmentSeq = sequence {
            patternRegex?.let { yield("custom pattern $it") }
            termsRegex?.let { yield("search term pattern $it") }
        }

        writer.newLine()
        when (optionsSegmentSeq.count() == 0) {
            false -> {
                "Searching beneath ${directory.absolutePath} for items matching " +
                        optionsSegmentSeq.joinToString(postfix = ".", separator = " and ")
            }
            true -> "Listing items beneath ${directory.absolutePath}."
        }.also { writer.write(it) }
        writer.newLine()
    }

    private fun getFilteredItems(
        directoryGraph: FileType.Directory,
        patternRegex: Regex?,
        termsRegex: Regex?
    ): List<FileType> {
        return directoryGraph.children?.values?.filter {
            patternRegex?.let { regex -> it.name.matches(regex) } ?: true
                    && termsRegex?.let { regex -> it.name.matches(regex) } ?: true
        } ?: listOf(directoryGraph)
    }

    private fun writeContents(writer: BufferedWriter, filteredItems: List<FileType>) {
        filteredItems.map {
            when (it) {
                is FileType.Directory -> {
                    writer.write(it.contents())
                    writer.newLine()
                }
            }
        }
    }
}

typealias JFile = File

sealed class FileType(val file: JFile) {
    val name: String = file.name

    abstract fun contents(): String

    class File(file: JFile) : FileType(file) {
        override fun contents(): String = file.name
    }

    class Directory(file: JFile) : FileType(file) {
        val children: Map<String, FileType>?

        init {
            children = file.listFiles()?.fold(emptyMap()) { acc, nested ->
                acc + nested.listFiles()?.run {
                    associate {
                        it.name to when (it.isDirectory) {
                            true -> Directory(it)
                            false -> File(it)
                        }
                    }
                }.orEmpty()
            }
        }

        val isLeaf = children?.values?.none { it.file.isDirectory } ?: true

        override fun contents(): String = when (isLeaf) {
            true -> "$name/"
            false -> "${file.absolutePath}: " + children!!.values.filter { !it.file.isDirectory }
                .joinToString(separator = ", ") { it.contents() }
        }
    }
}