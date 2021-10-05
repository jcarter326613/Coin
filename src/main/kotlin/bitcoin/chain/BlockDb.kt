package bitcoin.chain

import storage.IStorage
import storage.LocalStorage
import java.lang.Integer.max

/**
 * Responsible for representing the full active block chain.  As the chain grows, new blocks are added using the
 * function addBlock. If the top of the chain changes, the base of the part that needs to be changed should be removed
 * using the function removeBlock and then the new top should be added one at a time with addBlock in order of lower to
 * higher blocks.
 */
class BlockDb private constructor(private val storageController: IStorage) {
    var lastBlockHeight: Int = 0
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
            if (blockMapByHash.isNotEmpty()) {
                if (blockMapByHash[ByteArrayWrapper(block.previousBlockHash)] == null ||
                    !inMemoryBlocks.last().hash.contentEquals(block.previousBlockHash)
                ) {
                    return false
                }
            }

            // Add the block to the chain
            blockMapByHash[ByteArrayWrapper(block.hash)] = block
            inMemoryBlocks.add(block)
            lastBlockHeight++

            // Update the memory usage and age off the old blocks
            memorySizeUsed += block.memorySize
            while (memorySizeUsed > maxMemorySize) {
                val blockToRemove = inMemoryBlocks.removeFirst()
                memorySizeUsed -= blockToRemove.memorySize
                blockMapByHash.remove(ByteArrayWrapper(blockToRemove.hash))
            }

            // Add the block to storage
            //storageController.insertData(block.toByteArray(), block.hash)

            _locatorHashes = null
            return true
        }
    }

    fun getBlock(hash: ByteArray): Block? {
        return blockMapByHash[ByteArrayWrapper(hash)]
    }

    fun removeBlock(block: Block) {
        synchronized(blockMapByHash) {
            val levelIndex = inMemoryBlocks.indexOf(block)
            while (inMemoryBlocks.size > levelIndex) {
                val toRemove = inMemoryBlocks.removeAt(levelIndex)
                blockMapByHash.remove(ByteArrayWrapper(toRemove.hash))
                lastBlockHeight--
            }

            _locatorHashes = null
        }
    }

    companion object {
        var instance = BlockDb(LocalStorage())
            private set

        private const val maxMemorySize = 10 * 1024 * 1024  // 10 megabytes

        fun overrideDatabase(storage: IStorage): BlockDb {
            val instance = BlockDb(storage)
            this.instance = instance
            return instance
        }
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