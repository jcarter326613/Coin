package bitcoin.messages

import bitcoin.messages.components.NetworkAddress
import bitcoin.messages.components.VariableString
import util.ByteManipulation
import util.RangeShift
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class PingMessage(val nonce: Long) {
    fun toByteArray(): ByteArray {
        val array = ByteArray(8)
        ByteManipulation.writeLongToArray(nonce, array, 0)
        return array
    }

    companion object {
        fun fromByteArray(buffer: ByteArray): PingMessage {
            val nonce = ByteManipulation.readLongFromArray(buffer, 0)
            return PingMessage(nonce = nonce.value)
        }
    }
}