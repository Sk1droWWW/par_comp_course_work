package server

import java.io.*
import java.net.ServerSocket
import java.net.Socket
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

fun findWord(word: String): String {
    val w = word.toLowerCase()
    val searchResult: String
    val locations = invIndex[w]

    if (locations != null) {
        searchResult = locations.joinToString("\n") { "    $it" }
    }
    else {
        searchResult = "\n'$word' not found"
    }

    return searchResult + "\n"
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

fun indexCreatingParallel(threadsNumber: Int,
                          fileNamesList: MutableList<String>) {
    val threadArray: Array<ThreadIndex?> = arrayOfNulls(threadsNumber)
    val size = fileNamesList.size

    for (i in 0 until threadsNumber) {
        threadArray[i] = ThreadIndex(
            fileNamesList,
            (size / threadsNumber) * i,
            if (i == threadsNumber - 1) size else size / threadsNumber * (i + 1)
        )
        threadArray[i]!!.start()
    }

    for (i in 0 until threadsNumber) {
        threadArray[i]!!.join()
    }
}

fun main(args: Array<String>) {
    var serverSocket: ServerSocket? = null
    var clientSocket: Socket? = null

    try {
        serverSocket = ServerSocket(25445)
        println("Server started.")
    } catch (e: Exception) {
        System.err.println("Port already in use.")
        System.exit(1)
    }

    println("File names reading")
    val fileNamesList = getFileNamesList()

    indexCreatingParallel(16, fileNamesList)

    while (true) {
        try {
            clientSocket = serverSocket!!.accept()
            println("Accepted connection : $clientSocket")

            val t = Thread(ClientHandler(clientSocket))
            t.start()
        } catch (e: Exception) {
            System.err.println("Error in connection attempt.")
        }
    }
}


