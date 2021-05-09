package server

import java.io.File

class ThreadIndex(
    private val fileNamesList: MutableList<String>,
    private val startIndex: Int,
    private val endIndex: Int
) : Thread() {
    private val splitter = Regex("""\W+""")
    private val fileNames = mutableListOf<String>()

    override fun run() {
        for (i in startIndex until endIndex) {
            indexFile(fileNamesList[i])
        }
    }

    private fun indexFile(fileName: String) {
        if (fileName in fileNames) {
            println("'$fileName' already indexed")
            return
        }

        fileNames.add(fileName)
        File(fileName).forEachLine { line ->
            for ((i, w) in line.toLowerCase().split(splitter).withIndex()) {
                var locations = invIndex?.get(w)
                if (locations == null) {
                    locations = mutableListOf<Location>()
                    invIndex?.put(w, locations)
                }
                locations.add(Location(fileName, i + 1))
            }
        }

        println("'$fileName' has been indexed")
    }
}
