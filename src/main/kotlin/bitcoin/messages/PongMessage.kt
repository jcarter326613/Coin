package bitcoin.messages

import util.ByteManipulation

data class PongMessage(val nonce: Long) {
    fun toByteArray(): ByteArray {
        val array = ByteArray(8)
        ByteManipulation.writeLongToArray(nonce, array, 0)
        return array
    }

    companion object {
        fun fromByteArray(buffer: ByteArray): PongMessage {
            val nonce = ByteManipulation.readLongFromArray(buffer, 0)
            return PongMessage(nonce = nonce.value)
        }
    }
}