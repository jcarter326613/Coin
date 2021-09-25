package bitcoin.messages

import util.ByteManipulation

data class PongMessage(val nonce: Long) {
    fun toByteArray(): ByteArray {
        val array = ByteArray(8)
        ByteManipulation.writeLongToArray(nonce, array, 0)
        return array
    }
}