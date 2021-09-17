package bitcoin.messages

import bitcoin.messages.components.NetworkAddress
import bitcoin.messages.components.VariableString
import util.RangeShift
import java.nio.ByteBuffer
import java.nio.ByteOrder

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
        var currentOffset = 0

        ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(protocolVersion).array().copyInto(array)
        currentOffset += 4
        ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(services).array().copyInto(array, currentOffset)
        currentOffset += 8
        ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(timestamp).array().copyInto(array, 12)
        currentOffset += 8
        currentOffset = targetAddress.intoByteArray(array, currentOffset, false, true)
        currentOffset = sourceAddress.intoByteArray(array, currentOffset, false, false)
        ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(nonce).array().copyInto(array, currentOffset)
        currentOffset += 8
        currentOffset = userAgent.intoByteArray(array, currentOffset)
        ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(startHeight).array().copyInto(array, currentOffset)
        currentOffset += 4
        array[currentOffset] = if (relay) 1 else 0
        return array
    }

    private fun calculateMessageSize(): Int {
        var size = 4 + 8 + 8 + 8 + 4 + 1
        size += targetAddress.calculateMessageSize(false)
        size += sourceAddress.calculateMessageSize(false)
        size += userAgent.calculateMessageSize()
        return size
    }

    companion object {
        fun fromByteArray(buffer: ByteArray): VersionMessage {
            var currentRange = RangeShift.createRange(0, 4)
            val protocolVersion: Int = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).put(buffer.slice(currentRange.range).toByteArray()).getInt(0)
            currentRange = RangeShift.createRange(currentRange.nextI, 8)
            val services: Long = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).put(buffer.slice(currentRange.range).toByteArray()).getLong(0)
            currentRange = RangeShift.createRange(currentRange.nextI, 8)
            val timestamp: Long = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).put(buffer.slice(currentRange.range).toByteArray()).getLong(0)
            val targetAddress: NetworkAddress = NetworkAddress.fromByteArray(buffer, 20, false)
            val targetAddressBytes = targetAddress.calculateMessageSize(false)
            currentRange = RangeShift.createRange(currentRange.nextI, targetAddressBytes)
            val sourceAddress: NetworkAddress = NetworkAddress.fromByteArray(buffer, 20 + targetAddressBytes, false)
            val sourceAddressBytes = sourceAddress.calculateMessageSize(false)
            currentRange = RangeShift.createRange(currentRange.nextI, sourceAddressBytes)
            currentRange = RangeShift.createRange(currentRange.nextI, 8)
            val nonce: Long = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).put(buffer.slice(currentRange.range).toByteArray()).getLong(0)
            val userAgent: VariableString = VariableString.fromByteArray(buffer, 28 + targetAddressBytes + sourceAddressBytes)
            val userAgentBytes = userAgent.calculateMessageSize()
            currentRange = RangeShift.createRange(currentRange.nextI, userAgentBytes)
            currentRange = RangeShift.createRange(currentRange.nextI, 4)
            val startHeight: Int = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).put(buffer.slice(currentRange.range).toByteArray()).getInt(0)
            currentRange = RangeShift.createRange(currentRange.nextI, 1)
            val relay: Boolean = buffer[currentRange.range.first] != 0.toByte()

            return VersionMessage(
                protocolVersion = protocolVersion,
                services = services,
                timestamp = timestamp,
                targetAddress = targetAddress,
                sourceAddress = sourceAddress,
                nonce = nonce,
                userAgent = userAgent,
                startHeight = startHeight,
                relay = relay
            )
        }
    }
}