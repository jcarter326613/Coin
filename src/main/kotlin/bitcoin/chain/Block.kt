package bitcoin.chain

import bitcoin.messages.BlockHeaderMessage
import bitcoin.messages.BlockMessage
import bitcoin.messages.TxMessage
import bitcoin.messages.components.VariableInt
import kotlinx.coroutines.processNextEventInCurrentThread
import java.security.MessageDigest

class Block(
    val message: BlockMessage,
    var nextBlockHash: ByteArray? = null,
    height: Int? = null
) {
    private var _memorySize: Int? = null
    val memorySize: Int
        get() {
            var memorySize = _memorySize
            if (memorySize == null) {
                memorySize = message.calculateMessageSize()
                memorySize += (nextBlockHash?.let {it.size} ?: 0) + 1
                memorySize += 4    // Height int
                _memorySize = memorySize
            }
            return memorySize
        }

    val previousBlockHash: ByteArray get() = message.previousBlockHash

    private var _hash: ByteArray? = hash
    val hash: ByteArray
        get() {
            var hash = _hash
            if (hash == null) {
                val hasher = MessageDigest.getInstance("SHA-256")
                val header = message.toBlockHeaderMessage()
                val messageHeaderBytes = header.toByteArray()
                hash = hasher.digest(hasher.digest(messageHeaderBytes.sliceArray(0 until (messageHeaderBytes.size - 1))))
                _hash = hash
                return hash
            }
            return hash
        }

    var height: Int = height ?: 0

    init {
        if (message.transactions.isEmpty()) {
            throw Exception("Invalid number of transactions in block")
        }
    }

    fun toByteArray(): ByteArray {
        val array = ByteArray(calculateByteArraySize())
        var currentOffset = message.intoByteArray(array, 0)
        val nextBlockHash = nextBlockHash
        if (nextBlockHash == null) {
            array[currentOffset] = 0
            currentOffset++
        } else {
            array[currentOffset] = 1
            nextBlockHash.copyInto(array, currentOffset + 1)
            currentOffset += 1 + nextBlockHash.size
        }
        val heightInt = VariableInt(height.toLong())
        heightInt.intoByteArray(array, currentOffset)

        return array
    }

    fun calculateByteArraySize(): Int = message.calculateMessageSize() + 32 + 4

    companion object {
        fun fromByteArray(array: ByteArray): Block {
            val block = BlockMessage.fromByteArrayWithIndex(array, 0)
            val nextHashIndex: Int
            val nextHash: ByteArray?
            if (array[block.nextIndex] == 1.toByte()) {
                nextHash = array.slice((block.nextIndex + 1) until (block.nextIndex + 1 + 32)).toByteArray()
                nextHashIndex = block.nextIndex + 1 + 32
            } else {
                nextHash = null
                nextHashIndex = block.nextIndex + 1
            }
            val height = VariableInt.fromByteArray(array, nextHashIndex)

            return Block(block.value, nextHash, height.value.value.toInt())
        }
    }
}