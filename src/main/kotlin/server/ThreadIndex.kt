package server

class ThreadIndex(private val fileNamesList: MutableList<String>,
                  private val startIndex: Int,
                  private val endIndex: Int) : Thread() {

    override fun run() {
        for (i in startIndex until endIndex) {
            indexFile(fileNamesList[i])
        }
    }
}
