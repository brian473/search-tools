@file:Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package org.brianbrown.search

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.io.BufferedWriter
import java.io.File

private val logger = KotlinLogging.logger {}

open class FileSearch {
    fun search(directory: File, pattern: Regex? = null, outputStream: BufferedWriter = System.out.bufferedWriter()) {
        runBlocking {
            outputStream.use {
                writeContents(it, FileType.Directory(directory), pattern)
                it.close()
            }
        }
    }

    private fun writeContents(
        writer: BufferedWriter,
        rootDirectory: FileType.Directory,
        regex: Regex?
    ) {
        runBlocking {
            recurseDirectories(rootDirectory)
                .map { it.contents() }
                .filter { contents -> regex?.let { contents.contains(it) } ?: true }
                .forEach {
                    writer.write(it)
                    writer.newLine()
                    writer.flush()
                }
        }
    }

    private suspend fun recurseDirectories(root: FileType.Directory): List<FileType.Directory> {
        return listOf(root) + root.childDirectories
            .flatMapConcat { it.childDirectories }
            .filter { it.childFiles.count() != 0 }
            .toList()
    }
}

typealias JFile = File

sealed class FileType(val file: JFile) {
    val name: String = file.name

    abstract suspend fun contents(): String

    class File(file: JFile) : FileType(file) {
        override suspend fun contents(): String = file.name
    }

    class Directory(file: JFile) : FileType(file) {
        val childDirectories: Flow<Directory>
        val childFiles: Flow<File>

        init {
            val children = fetchChildren(file)
            childFiles = children.filterIsInstance()
            childDirectories = children.filterIsInstance()
        }

        private fun fetchChildren(file: JFile) = flow {
            file.listFiles()?.let { files ->
                files.map { child ->
                    coroutineScope {
                        async {
                            when (child.isDirectory) {
                                true -> Directory(child)
                                false -> File(child)
                            }
                        }
                    }
                }.map {
                    emit(it.await())
                }
            }
        }

        override suspend fun contents(): String {
            return "${file.absolutePath}: " + childFiles
                .toList()
                .joinToString(", ") { it.name }
        }

    }
}