import java.io.File

val invIndex  = mutableMapOf<String, MutableList<Location>>()
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

fun main(args: Array<String>) {
    val PATH: String = "src/main/resources/"
    println("Enter file names")

    val stringInput: String = readLine()!!

    for (arg in stringInput.split(" ")) indexFile(PATH + arg)
    println()
    println("Enter word(s) to be searched for in these files or 'q' to quit")
    while (true) {
        print("  ? : ")
        val word = readLine()!!
        if (word.toLowerCase() == "q") return
        findWord(word)
    }
}

