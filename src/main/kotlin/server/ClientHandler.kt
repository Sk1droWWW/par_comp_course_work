package server

import java.io.*
import java.net.Socket


class ClientHandler(private val clientSocket: Socket) : Runnable {
    override fun run() {
        val dis = DataInputStream(clientSocket.getInputStream())
        val dos = DataOutputStream(clientSocket.getOutputStream())

        try {
            var clientSelection: String
            while (true) {
                println("Read input from client : $clientSocket")
                clientSelection = dis.readUTF()

                clientSelection = clientSelection.toLowerCase()
                if (clientSelection == "q") {
                    System.exit(1)
                } else {
                    var result = "\n'$clientSelection' found in the following locations:"
                    result += findWord(clientSelection)

                    sendSearchResult(result, dos)
                    println("Search result send to : $clientSocket")
                }
            }
        } catch (ex: IOException) {
            println("Connection closed : $clientSocket")
        }

        try {
            dis.close()
            dos.close()
        } catch (ex: IOException) {
            System.err.print(ex.printStackTrace().toString())
        }
    }

    private fun findWord(word: String): String {
        val w = word.toLowerCase()
        val searchResult: String
        val locations = invIndex?.get(w)

        searchResult = locations?.joinToString("\n") { "    $it" }
            ?: "\n'$word' not found"

        return searchResult + "\n"
    }

    private fun sendSearchResult(searchResult: String, dos: DataOutputStream) {
        try {
            val myByteArray = ByteArray(searchResult.length)

            val bis = BufferedInputStream(ByteArrayInputStream(searchResult.encodeToByteArray()))
            val dis = DataInputStream(bis)

            dis.readFully(myByteArray, 0, myByteArray.size)

            dos.writeLong(myByteArray.size.toLong())
            dos.write(myByteArray, 0, myByteArray.size)
        } catch (e: Exception) {
            System.err.println("Exception: $e")
        }
    }
}