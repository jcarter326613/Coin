package bitcoin.chain

import storage.IStorage
import storage.LocalStorage

/**
 * Responsible for representing the full active block chain.  As the chain grows, new blocks are added using the
 * function addBlock. If the top of the chain changes, the base of the part that needs to be changed should be removed
 * using the function removeBlock and then the new top should be added one at a time with addBlock in order of lower to
 * higher blocks.
 */
class BlockDb private constructor(private val storageController: IStorage) {
    val lastBlockHeight: Int = 0

    private val blockMapByHash = mutableMapOf<ByteArray, Block>()
    private val inMemoryBlocks = mutableListOf<Block>()
    private var memorySizeUsed = 0

    fun addBlock(block: Block) {
        synchronized(blockMapByHash) {
            if (blockMapByHash[block.previousBlockHash] == null ||
                !inMemoryBlocks.last().hash.contentEquals(block.previousBlockHash)) {
                throw Exception("Can not add block when previous block is not in chain")
            }
            blockMapByHash[block.hash] = block
            inMemoryBlocks.add(block)

            // Update the memory usage and age off the old blocks
            memorySizeUsed += block.memorySize
            while (memorySizeUsed > maxMemorySize) {
                val blockToRemove = inMemoryBlocks.removeFirst()
                memorySizeUsed -= blockToRemove.memorySize
                blockMapByHash.remove(blockToRemove.hash)
            }

            // Add the block to storage
            //storageController.insertData(block.toByteArray(), block.hash)
        }
    }

    fun getBlock(hash: ByteArray): Block? {
        return blockMapByHash[hash]
    }

    fun removeBlock(block: Block) {
        synchronized(blockMapByHash) {
            val levelIndex = inMemoryBlocks.indexOf(block)
            while (inMemoryBlocks.size > levelIndex) {
                val toRemove = inMemoryBlocks.removeAt(levelIndex)
                blockMapByHash.remove(toRemove.hash)
            }
        }
    }

    companion object {
        val instance = BlockDb(LocalStorage())

        private const val maxMemorySize = 10 * 1024 * 1024
    }
}