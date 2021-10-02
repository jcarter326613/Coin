package bitcoin.chain

class BlockDb private constructor() {
    val lastBlock: Int = 0

    fun addBlock(block: Block) {

    }

    fun getBlock(hash: ByteArray): Block? {
        return null
    }

    companion object {
        val instance = BlockDb()
    }
}