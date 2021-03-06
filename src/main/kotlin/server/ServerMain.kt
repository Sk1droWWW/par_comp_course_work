package server

import java.io.File
import java.net.ServerSocket
import java.net.Socket
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.system.measureTimeMillis


var invIndex: ConcurrentHashMap<String, MutableList<Location>>? = null
val pathList = arrayListOf(
    "src/main/resources/test/neg/",
    "src/main/resources/test/pos/",
    "src/main/resources/train/neg/",
    "src/main/resources/train/pos/",
    "src/main/resources/train/unsup/"
)

fun getFileNamesList(pathList: List<String>): MutableList<String> {
    val results: MutableList<String> = ArrayList()

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

fun indexCreatingParallel(
    threadsNumber: Int,
    fileNamesList: MutableList<String>,
    index: ConcurrentHashMap<String, MutableList<Location>>?
) {
    val threadArray: Array<ThreadIndex?> = arrayOfNulls(threadsNumber)
    val size = fileNamesList.size

    for (i in 0 until threadsNumber) {
        threadArray[i] = ThreadIndex(
            fileNamesList,
            index,
            (size / threadsNumber) * i,
            if (i == threadsNumber - 1) size else size / threadsNumber * (i + 1)
        )
        threadArray[i]!!.start()
    }

    for (i in 0 until threadsNumber) {
        threadArray[i]!!.join()
    }
}

fun main() {
    var serverSocket: ServerSocket? = null
    var clientSocket: Socket? = null

    try {
        serverSocket = ServerSocket(25445)
        println("Server started")
    } catch (e: Exception) {
        System.err.println("Port already in use")
        System.exit(1)
    }

    println("File names reading")
    val fileNamesList = getFileNamesList(pathList)

    println("Enter number of threads: ")
    val threadsNumber = Scanner(System.`in`).nextInt()

    invIndex = ConcurrentHashMap<String,
            MutableList<Location>>(16, 0.75f, threadsNumber)
    val timeInMillis = measureTimeMillis {
        indexCreatingParallel(threadsNumber, fileNamesList, invIndex)
    }
    println("Index created by $threadsNumber threads in : $timeInMillis millis")

    while (true) {
        try {
            clientSocket = serverSocket!!.accept()
            println("Accepted connection : $clientSocket")

            val t = Thread(ClientHandler(clientSocket))
            t.start()
        } catch (e: Exception) {
            System.err.println("Error in connection attempt")
        }
    }
}


