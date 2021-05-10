import server.Location
import server.getFileNamesList
import server.indexCreatingParallel
import java.util.concurrent.ConcurrentHashMap
import kotlin.system.measureTimeMillis

const val MAX_THREADS_NUMBER = 100

fun main() {
    println("File names reading")
    val fileNamesList = getFileNamesList()

    var results = "Index created by 1 thread in : ${invIndexCreating(fileNamesList, 1)} sec\n"
    for (i in 2..MAX_THREADS_NUMBER step 4) {
        results += "Index created by $i threads in : ${invIndexCreating(fileNamesList, i)} millis\n"
    }
    println(results)
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