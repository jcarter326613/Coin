package bitcoin

class BlockDb private constructor() {
    val lastBlock = 0

    companion object {
        val instance = BlockDb()
    }
}