package org.brianbrown.search

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.options.Option

class SearchOptions {
    @Option(
        option = "pattern",
        description = "Sets a regex pattern which all output must match. This takes precedence over search terms."
    )
    @Input
    lateinit var pattern: String

    @Option(
        option = "terms",
        description = "Set a comma separated list of String literal search terms. If set, output will " +
                "exclude entries which do not contain any of the specified terms."
    )
    @Input
    lateinit var terms: String

    @Option(
        option = "rootDir",
        description = "Sets the directory from which the search will recursively traverse depth-wise."
    )
    @Input
    var rootDir: String = "/"
}