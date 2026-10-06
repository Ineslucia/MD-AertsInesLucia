//Build a Song Class with: title, artist, yearPublished, playCount, isPopular, a printDescription() method.
//Properties for the title, artist, year published, and play count
//A property that indicates whether the song is popular. If the play count is less than 1,000, consider it unpopular.
//A method that prints a song description in this format:
//"[Title], performed by [artist], was released in [year published]."

fun main() {
    val song1 = Song("Imagine", "John Lennon", 1971, 1500)
    val song2 = Song("Happy Birthday", "Unknown", 1893, 500)
    val song3 = Song("Smells Like Teen Spirit", "Nirvana", 1991, 5000000)

    song1.printDescription()
    song2.printDescription()
    song3.printDescription()

    println(song1.isPopular)
    println(song2.isPopular)
    println(song3.isPopular)
}




class Song(
    val title: String,
    val artist: String,
    val yearPublished: Int,
    val playCount: Int
) {

    val isPopular: Boolean
        get() = playCount >= 1000

    fun printDescription() {
        println("$title, performed by $artist, was released in $yearPublished.")
    }
}
