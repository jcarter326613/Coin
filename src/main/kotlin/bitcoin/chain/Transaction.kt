package bitcoin.chain

import bitcoin.messages.TxMessage
import java.security.MessageDigest

class Transaction(
    val message: TxMessage,
    val sourceBlockHash: ByteArray
) {
    private var _hash: ByteArray? = hash
    val hash: ByteArray
        get() {
            var hash = _hash
            if (hash == null) {
                val hasher = MessageDigest.getInstance("SHA-256")
                val messageBytes = message.toByteArray()
                hash = hasher.digest(hasher.digest(messageBytes.sliceArray(0 until (messageBytes.size - 1))))
                _hash = hash
                return hash
            }
            return hash
        }
}