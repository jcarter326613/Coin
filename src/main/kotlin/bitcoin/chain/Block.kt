package bitcoin.chain

import bitcoin.messages.BlockHeaderMessage
import bitcoin.messages.BlockMessage
import bitcoin.messages.TxMessage
import kotlinx.coroutines.processNextEventInCurrentThread
import java.security.MessageDigest

class Block(
    val message: BlockMessage,
    var nextBlockHash: ByteArray? = null,
    height: Int? = null,
    hash: ByteArray? = null,
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

    var height: Int = 0

    init {
        if (message.transactions.isEmpty()) {
            throw Exception("Invalid number of transactions in block")
        }
    }
}