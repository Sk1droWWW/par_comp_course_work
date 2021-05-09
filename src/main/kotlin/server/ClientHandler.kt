package server

import java.io.*
import java.net.Socket


class ClientHandler(private val clientSocket: Socket) : Runnable {
    private var os: PrintStream? = null
    private var `in`: BufferedReader? = null

    override fun run() {
        try {
            os = PrintStream(clientSocket.getOutputStream())
            `in` = BufferedReader(InputStreamReader(clientSocket.getInputStream()))

            var clientSelection: String
            while (`in`!!.readLine().also { clientSelection = it } != null) {
                println("Read input from client $clientSocket")

                clientSelection = clientSelection.toLowerCase()
                if (clientSelection == "q") {
                    System.exit(1)
                } else {
                    var result = "\n'$clientSelection' found in the following locations:"
                    result += findWord(clientSelection)

                    sendSearchResult(result, clientSocket)
                }
            }
        } catch (ex: IOException) {
        }
    }

    private fun sendSearchResult(searchResult: String, clientSocket: Socket) {
        try {
            val myByteArray = ByteArray(searchResult.length)
            val bis = BufferedInputStream(ByteArrayInputStream(searchResult.encodeToByteArray()))
            val dis = DataInputStream(bis)
            dis.readFully(myByteArray, 0, myByteArray.size)

            val os = clientSocket.getOutputStream()
            val dos = DataOutputStream(os)
            dos.writeLong(myByteArray.size.toLong())
            dos.write(myByteArray, 0, myByteArray.size)
            dos.flush()

            println("Search result send to $clientSocket")
        } catch (e: Exception) {
            System.err.println("Exception: $e")
        }
    }
}