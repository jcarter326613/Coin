package bitcoin.chain

import bitcoin.messages.BlockHeaderMessage
import bitcoin.messages.BlockMessage
import kotlinx.coroutines.processNextEventInCurrentThread
import java.security.MessageDigest

class Block(val message: BlockMessage) {
    private var _hash: ByteArray? = null
    val hash: ByteArray
        get() {
            var hash = _hash
            if (hash == null) {
                val hasher = MessageDigest.getInstance("SHA-256")
                val header = BlockHeaderMessage(
                    version = message.version,
                    previousBlockHash = message.previousBlockHash,
                    merkleRootHash = message.merkleRootHash,
                    timestamp = message.timestamp,
                    difficultyTarget = message.difficultyTarget,
                    nonce = message.nonce
                )
                val messageHeaderBytes = header.toByteArray()
                hash = hasher.digest(messageHeaderBytes.sliceArray(0 until (messageHeaderBytes.size - 1)))
                _hash = hash
                return hash
            }
            return hash
        }

    private var _height: Int? = null
    val height: Int
        get() {
            var height = _height
            if (height == null) {
                val previousBlock = BlockDb.instance.getBlock(message.previousBlockHash) ?: throw Exception("Could not retrieve previous block")
                height = previousBlock.height + 1
                _height = height
                return height
            }
            return height
        }
}