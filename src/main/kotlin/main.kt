import java.io.File
import java.util.ArrayList
import java.util.concurrent.ConcurrentHashMap


val invIndex  = ConcurrentHashMap<String, MutableList<Location>>(16, 0.75f, 16)
val fileNames = mutableListOf<String>()
val splitter  = Regex("""\W+""")

class Location(val fileName: String, val wordNum: Int) {
    override fun toString() = "{$fileName, word number $wordNum}"
}

fun indexFile(fileName: String) {
    if (fileName in fileNames) {
        println("'$fileName' already indexed")
        return
    }

    fileNames.add(fileName)
    File(fileName).forEachLine { line ->
        for ((i, w) in line.toLowerCase().split(splitter).withIndex()) {
            var locations = invIndex[w]
            if (locations == null) {
                locations = mutableListOf<Location>()
                invIndex.put(w, locations)
            }
            locations.add(Location(fileName, i + 1))
        }
    }
    println("'$fileName' has been indexed")
}

fun findWord(word: String) {
    val w = word.toLowerCase()
    val locations = invIndex[w]
    if (locations != null) {
        println("\n'$word' found in the following locations:")
        println(locations.map { "    $it" }.joinToString("\n"))
    }
    else println("\n'$word' not found")
    println()
}

fun getFileNamesList(): MutableList<String> {
    val results: MutableList<String> = ArrayList()
    val pathList = arrayListOf(
        "src/main/resources/test/neg/",
        "src/main/resources/test/pos/",
        "src/main/resources/train/neg/",
        "src/main/resources/train/pos/",
        "src/main/resources/train/unsup/")

    for (p in pathList) {
        val files = File(p).listFiles()

        for (f in files) {
            if (f.isFile) {
                results.add(p + f.name)
            }
        }
    }

    return results
}

fun indexCreatingParallel(threads_number: Int,
                          fileNamesList: MutableList<String>) {
    val threadArray: Array<ThreadIndex?> = arrayOfNulls(threads_number)
    val size = fileNamesList.size

    for (i in 0 until threads_number) {
        threadArray[i] = ThreadIndex(
            fileNamesList,
            (size / threads_number) * i,
            if (i == threads_number - 1) size else size / threads_number * (i + 1)
        )
        threadArray[i]!!.start()
    }

    for (i in 0 until threads_number) {
        threadArray[i]!!.join()
    }
}

fun main(args: Array<String>) {
    println("File names reading")

    val fileNamesList = getFileNamesList()

    indexCreatingParallel(16, fileNamesList)
    /*for (fn in fileNames) {
        indexFile(fn)
    }*/
    println()
    println("Enter word(s) to be searched for in these files or 'q' to quit")
    while (true) {
        print("  ? : ")
        val word = readLine()!!
        if (word.toLowerCase() == "q") return
        findWord(word)
    }
}


