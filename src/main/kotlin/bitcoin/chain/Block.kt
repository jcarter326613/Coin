package bitcoin.chain

import bitcoin.messages.BlockHeaderMessage
import bitcoin.messages.BlockMessage
import bitcoin.messages.TxMessage
import kotlinx.coroutines.processNextEventInCurrentThread
import java.security.MessageDigest

class Block(
    val version: Int,
    val previousBlockHash: ByteArray,
    var nextBlockHash: ByteArray? = null,
    val merkleRootHash: ByteArray,
    val timestamp: Int,
    val difficultyTarget: Int,
    val nonce: Int,
    val transactions: List<TxMessage>,
    val memorySize: Int,
    height: Int? = null,
    hash: ByteArray? = null,
) {
    private var _hash: ByteArray? = hash
    val hash: ByteArray
        get() {
            var hash = _hash
            if (hash == null) {
                val hasher = MessageDigest.getInstance("SHA-256")
                val header = BlockHeaderMessage(
                    version = version,
                    previousBlockHash = previousBlockHash,
                    merkleRootHash = merkleRootHash,
                    timestamp = timestamp,
                    difficultyTarget = difficultyTarget,
                    nonce = nonce
                )
                val messageHeaderBytes = header.toByteArray()
                hash = hasher.digest(messageHeaderBytes.sliceArray(0 until (messageHeaderBytes.size - 1)))
                _hash = hash
                return hash
            }
            return hash
        }

    private var _height: Int? = height
    val height: Int
        get() {
            var height = _height
            if (height == null) {
                val previousBlock = BlockDb.instance.getBlock(previousBlockHash) ?: throw Exception("Could not retrieve previous block")
                height = previousBlock.height + 1
                _height = height
                return height
            }
            return height
        }

    init {
        if (transactions.isEmpty()) {
            throw Exception("Invalid number of transactions in block")
        }
    }

    companion object {
        fun fromMessage(message: BlockMessage): Block {
            return Block(
                version = message.version,
                previousBlockHash = message.previousBlockHash,
                merkleRootHash = message.merkleRootHash,
                timestamp = message.timestamp,
                difficultyTarget = message.difficultyTarget,
                nonce = message.nonce,
                transactions = message.transactions,
                memorySize = message.calculateMessageSize()
            )
        }
    }
}