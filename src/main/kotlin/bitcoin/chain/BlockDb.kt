package bitcoin.chain

import bitcoin.messages.BlockMessage
import storage.IStorage
import storage.LocalStorage
import java.lang.Integer.max
import java.time.ZonedDateTime

/**
 * Responsible for representing the full active block chain.  As the chain grows, new blocks are added using the
 * function addBlock. If the top of the chain changes, the base of the part that needs to be changed should be removed
 * using the function removeBlock and then the new top should be added one at a time with addBlock in order of lower to
 * higher blocks.
 */
class BlockDb(private val storageController: IStorage, private val transactionDb: TransactionDb) {
    var lastBlockHeight: Int = 0
        private set
    var lastBlockReceived = ZonedDateTime.now().minusYears(1)
        private set

    private var _locatorHashes: MutableList<ByteArray>? = null
    val locatorHashes: List<ByteArray>
        get() {
            var locatorHashes = _locatorHashes
            if (locatorHashes == null) {
                locatorHashes = mutableListOf()
                val minConsecutiveIndex = max(inMemoryBlocks.size - 10, 0)
                for (i in (inMemoryBlocks.size - 1) downTo minConsecutiveIndex) {
                    locatorHashes.add(inMemoryBlocks[i].hash)
                }
                var step = 2
                var currentIndex = minConsecutiveIndex - step
                while (currentIndex > 0) {
                    locatorHashes.add(inMemoryBlocks[currentIndex].hash)
                    step *= 2
                    currentIndex -= step
                }
                _locatorHashes = locatorHashes
            }
            return locatorHashes
        }

    private val blockMapByHash = mutableMapOf<ByteArrayWrapper, Block>()
    private val inMemoryBlocks = mutableListOf<Block>()
    private var memorySizeUsed = 0

    fun addBlock(block: Block): Boolean {
        synchronized(blockMapByHash) {
            // Check that the block can be added to the chain
            if (inMemoryBlocks.lastOrNull()?.hash?.contentEquals(block.previousBlockHash) == false) {
                return false
            }

            // Add the block to the chain
            val previousLastBlock = inMemoryBlocks.lastOrNull()
            if (previousLastBlock == null) {
                block.height = 0
            } else {
                previousLastBlock.nextBlockHash = block.hash
                block.height = previousLastBlock.height + 1
            }
            lastBlockHeight++

            addBlockToMemoryCache(block)

            // Add the block to storage
            storageController.insertData(block.toByteArray(), block.hash)

            // Remove used transactions from the database
            for (transaction in block.message.transactions) {
                transactionDb.removeSources(transaction)
            }

            // Add the new transactions to the database
            for (transaction in block.message.transactions) {
                transactionDb.addOutputs(transaction)
            }

            _locatorHashes = null
            lastBlockReceived = ZonedDateTime.now()
            return true
        }
    }

    fun getBlock(hash: ByteArray): Block? {
        val cachedBlock = blockMapByHash[ByteArrayWrapper(hash)]
        if (cachedBlock == null) {
            val blockData = storageController.retrieveData(hash)
            val blockMessage = BlockMessage.fromByteArray(blockData)
            val block = Block(blockMessage, nextBlockHash, height, hash)

            addBlockToMemoryCache(block)
        }
        return cachedBlock
    }

    fun removeBlock(block: Block) {
        synchronized(blockMapByHash) {
            val levelIndex = inMemoryBlocks.indexOf(block)
            if (levelIndex >= 0) {
                // Null out the next hash of the previous block
                if (levelIndex > 0) {
                    inMemoryBlocks[levelIndex - 1].nextBlockHash = null
                }

                // Remove everything after this block in the chain and this block
                while (inMemoryBlocks.size > levelIndex) {
                    val toRemove = inMemoryBlocks.removeAt(levelIndex)
                    blockMapByHash.remove(ByteArrayWrapper(toRemove.hash))
                    lastBlockHeight--
                }

                _locatorHashes = null
            }
        }
    }

    private fun addBlockToMemoryCache(block: Block) {
        blockMapByHash[ByteArrayWrapper(block.hash)] = block
        inMemoryBlocks.add(block)

        // Update the memory usage and age off the old blocks
        memorySizeUsed += block.memorySize
        while (memorySizeUsed > maxMemorySize) {
            val blockToRemove = inMemoryBlocks.removeFirst()
            memorySizeUsed -= blockToRemove.memorySize
            blockMapByHash.remove(ByteArrayWrapper(blockToRemove.hash))
        }
    }

    companion object {
        private const val maxMemorySize = 10 * 1024 * 1024
    }

    private class ByteArrayWrapper(val array: ByteArray) {
        private var _hashCode: Int? = null

        override fun equals(other: Any?): Boolean {
            if (other !is ByteArrayWrapper) {
                return false
            }
            return array.contentEquals(other.array)
        }

        override fun hashCode(): Int {
            var hashCode = _hashCode
            if (hashCode == null) {
                hashCode = array.contentHashCode()
                _hashCode = hashCode
            }
            return hashCode
        }

        override fun toString(): String {
            return array.toString()
        }
    }
}