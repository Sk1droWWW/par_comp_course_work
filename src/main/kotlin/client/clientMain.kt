package client

import java.io.*
import java.net.Socket
import kotlin.math.min


var sock: Socket? = null
var stdin: BufferedReader? = null
var os: PrintStream? = null

private fun receiveSearchResult(): String? {
    var searchResult: String? = null
    try {
        var bytesRead: Int
        val clientData = DataInputStream(sock!!.getInputStream())
        var size = clientData.readLong()
        val output: OutputStream = ByteArrayOutputStream(size.toInt())
        val buffer = ByteArray(1024)

        bytesRead = clientData.read(buffer, 0, min(buffer.size.toLong(), size).toInt())

        while (size > 0 && bytesRead != -1) {
            output.write(buffer, 0, bytesRead)
            size -= bytesRead.toLong()

            bytesRead = clientData.read(buffer, 0, min(buffer.size.toLong(), size).toInt())
        }
        searchResult = output.toString()

        output.close()
        clientData.close()
    } catch (ex: IOException) {
        System.err.println("Client error. Connection closed.")
    }

    return searchResult
}

fun main(args: Array<String>) {
    while (true) {
        try {
            sock = Socket("localhost", 25445)
            stdin = BufferedReader(InputStreamReader(System.`in`))
        } catch (e: Exception) {
            System.err.println("Cannot connect to the server, try again later.")
            System.exit(1)
        }

        os = PrintStream(sock!!.getOutputStream())

        try {
            println("Enter word(s) to be searched for in index or 'q' to quit")
            while (true) {
                print("  ? : ")
                val word = stdin!!.readLine()

                if (word.toLowerCase() != "q") {
                    os!!.println(word)
                    print(receiveSearchResult())
                } else {
                    System.exit(1)
                }
            }
        } catch (e: Exception) {
            System.err.println("not valid input")
        }
    }
}
