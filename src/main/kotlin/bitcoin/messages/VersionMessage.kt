package bitcoin.messages

import bitcoin.messages.components.NetworkAddress
import bitcoin.messages.components.VariableString
import java.nio.ByteBuffer

data class VersionMessage(
    val protocolVersion: Int,
    val services: Long,
    val timestamp: Long,
    val targetAddress: NetworkAddress,
    val sourceAddress: NetworkAddress,
    val nonce: Long,
    val userAgent: VariableString,
    val startHeight: Int,
    val relay: Boolean
) {
    fun toByteArray(): ByteArray {
        val array = ByteArray(calculateMessageSize())

        ByteBuffer.allocate(4).putInt(protocolVersion).array().copyInto(array)
        ByteBuffer.allocate(8).putLong(services).array().copyInto(array, 4)
        ByteBuffer.allocate(8).putLong(timestamp).array().copyInto(array, 12)

        val targetAddressNumBytes = targetAddress.intoByteArray(array, 20)
        val sourceAddressNumBytes = sourceAddress.intoByteArray(array, 20 + targetAddressNumBytes)

        ByteBuffer.allocate(8).putLong(nonce).array().copyInto(array, 20 + targetAddressNumBytes + sourceAddressNumBytes)

        val userAgentNumBytes = userAgent.intoByteArray(array, 28 + targetAddressNumBytes + sourceAddressNumBytes)

        ByteBuffer.allocate(4).putInt(startHeight).array().copyInto(array,
            28 + targetAddressNumBytes + sourceAddressNumBytes + userAgentNumBytes)
        array[32 + targetAddressNumBytes + sourceAddressNumBytes + userAgentNumBytes] = if (relay) 1 else 0
        return array
    }

    private fun calculateMessageSize(): Int {
        var size = 32
        size += targetAddress.calculateMessageSize()
        size += sourceAddress.calculateMessageSize()
        size += userAgent.calculateMessageSize()
        return size
    }

    companion object {
        fun fromByteArray(buffer: ByteArray): VersionMessage {

        }
    }
}