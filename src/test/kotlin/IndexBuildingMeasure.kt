import server.Location
import server.getFileNamesList
import server.indexCreatingParallel
import server.pathList
import java.util.concurrent.ConcurrentHashMap
import kotlin.system.measureTimeMillis

const val MAX_THREADS_NUMBER = 60

fun main() {
    var results = ""
    var columnNames = "Threads number"

    results += "1"
    for (i in 1..5) {
        val fileNamesList = getFileNamesList(pathList.takeLast(i))
        columnNames += "\t${fileNamesList.size} files"

        results += "\t${invIndexCreating(fileNamesList, 1)}"
    }
    results += "\n"

    for (j in 2..MAX_THREADS_NUMBER step 4) {
        results += "$j"
        for (i in 1..5) {
            val fileNamesList = getFileNamesList(pathList.takeLast(i))
            results += "\t${invIndexCreating(fileNamesList, j)}"
        }
        results += "\n"
    }

    println(columnNames + "\n" + results)
}

private fun invIndexCreating(
    fileNamesList: MutableList<String>,
    threadsNumber: Int
): Long {
    val measureAmount: Int = 10

    var timeInMillis: Long = 0
    for (i in 0..measureAmount) {
        timeInMillis += singleInvIndexBuildTimeMeasure(fileNamesList, threadsNumber)
    }

    return timeInMillis / measureAmount
}

private fun singleInvIndexBuildTimeMeasure(
    fileNamesList: MutableList<String>,
    threadsNumber: Int,
): Long {
    val testInvIndex = ConcurrentHashMap<String,
            MutableList<Location>>(16, 0.75f, threadsNumber)
    return measureTimeMillis { indexCreatingParallel(threadsNumber, fileNamesList, testInvIndex) }
}