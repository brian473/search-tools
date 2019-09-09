import mu.KotlinLogging
import org.brianbrown.search.FileSearch
import org.junit.jupiter.api.Test

class FileSearchTest {

    val logger = KotlinLogging.logger(FileSearch::class.java.name)

    @Test
    fun testSearch() {
        val fileSearch = FileSearch()
        fileSearch.search("/etc")

//        val project = ProjectBuilder.builder().build()
//        val task = project.tasks.register("file-search", FileSearch::class.java)
//        assertTrue(task.isPresent)
//        assertTrue(task is FileSearch)
//        task.configure {
//            rootDir = "/etc"
//        }
    }
}