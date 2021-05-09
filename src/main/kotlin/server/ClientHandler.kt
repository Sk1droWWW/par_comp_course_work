package server

import java.io.*
import java.net.Socket
import java.io.DataOutputStream
import java.io.DataInputStream


class ClientHandler(private val clientSocket: Socket) : Runnable {
    override fun run() {
        val dis = DataInputStream(clientSocket.getInputStream())
        val dos = DataOutputStream(clientSocket.getOutputStream())

        try {
            var clientSelection: String
            while (true) {
                clientSelection = dis.readUTF()
                println("Read input from client : $clientSocket")

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
        } catch(ex: IOException) {
            System.err.print(ex.printStackTrace().toString())
        }
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