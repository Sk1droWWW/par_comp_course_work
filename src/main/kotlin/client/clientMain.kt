package client

import java.io.*
import java.net.Socket
import kotlin.math.min


var sock: Socket? = null
var stdin: BufferedReader? = null

private fun receiveSearchResult(dis: DataInputStream): String? {
    var searchResult: String? = null
    try {
        var bytesRead: Int
        var size = dis.readLong()
        val output: OutputStream = ByteArrayOutputStream(size.toInt())
        val buffer = ByteArray(1024)

        bytesRead = dis.read(buffer, 0, min(buffer.size.toLong(), size).toInt())

        while (size > 0 && bytesRead != -1) {
            output.write(buffer, 0, bytesRead)
            size -= bytesRead.toLong()

            bytesRead = dis.read(buffer, 0, min(buffer.size.toLong(), size).toInt())
        }
        searchResult = output.toString()

        output.close()
    } catch (ex: IOException) {
        System.err.println("Client error. Connection closed.")
    }

    return searchResult
}

fun main(args: Array<String>) {
    try {
        sock = Socket("localhost", 25445)
        stdin = BufferedReader(InputStreamReader(System.`in`))
    } catch (e: Exception) {
        System.err.println("Cannot connect to the server, try again later.")
        System.exit(1)
    }

    val dis = DataInputStream(sock!!.getInputStream())
    val dos = DataOutputStream(sock!!.getOutputStream())

    while (true) {
        try {
            println("Enter word(s) to be searched for in index or 'q' to quit")
            while (true) {
                print("  ? : ")
                val word = stdin!!.readLine()

                if (word.toLowerCase() != "q") {
                    dos.writeUTF(word)
                    print(receiveSearchResult(dis))
                } else {
                    sock!!.close()
                    System.exit(1)
                }
            }
        } catch (e: Exception) {
            System.err.println("not valid input")
        }

        try {
            dis.close()
            dos.close()
        } catch(ex: IOException) {
            System.err.print(ex.printStackTrace().toString())
        }
    }
}
