package server

class Location(
    private val fileName: String,
    private val wordNum: Int
) {
    override fun toString() = "{$fileName, word number $wordNum}"
}