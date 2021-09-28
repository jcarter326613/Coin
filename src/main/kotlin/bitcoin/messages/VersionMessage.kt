package bitcoin.messages

import bitcoin.messages.components.NetworkAddress
import bitcoin.messages.components.VariableString
import util.ByteManipulation
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

        currentOffset = ByteManipulation.writeIntToArray(protocolVersion, array, currentOffset)
        currentOffset = ByteManipulation.writeLongToArray(services, array, currentOffset)
        currentOffset = ByteManipulation.writeLongToArray(timestamp, array, currentOffset)
        currentOffset = targetAddress.intoByteArray(array, currentOffset, false)
        currentOffset = sourceAddress.intoByteArray(array, currentOffset, false)
        currentOffset = ByteManipulation.writeLongToArray(nonce, array, currentOffset)
        currentOffset = userAgent.intoByteArray(array, currentOffset)
        currentOffset = ByteManipulation.writeIntToArray(startHeight, array, currentOffset)
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
            val protocolVersion = ByteManipulation.readIntFromArray(buffer, 0, ByteOrder.LITTLE_ENDIAN)
            val services = ByteManipulation.readLongFromArray(buffer, protocolVersion.nextIndex, ByteOrder.LITTLE_ENDIAN)
            val timestamp = ByteManipulation.readLongFromArray(buffer, services.nextIndex, ByteOrder.LITTLE_ENDIAN)
            val targetAddress = NetworkAddress.fromByteArray(buffer, timestamp.nextIndex, false)
            val targetAddressBytes = targetAddress.calculateMessageSize(false)
            val sourceAddress = NetworkAddress.fromByteArray(buffer, timestamp.nextIndex + targetAddressBytes, false)
            val sourceAddressBytes = sourceAddress.calculateMessageSize(false)
            val nonce = ByteManipulation.readLongFromArray(buffer, timestamp.nextIndex + targetAddressBytes + sourceAddressBytes, ByteOrder.LITTLE_ENDIAN)
            val userAgent = VariableString.fromByteArray(buffer, nonce.nextIndex)
            val startHeight = ByteManipulation.readIntFromArray(buffer, userAgent.nextIndex, ByteOrder.LITTLE_ENDIAN)
            val relay: Boolean = buffer[startHeight.nextIndex] != 0.toByte()

            return VersionMessage(
                protocolVersion = protocolVersion.value,
                services = services.value,
                timestamp = timestamp.value,
                targetAddress = targetAddress,
                sourceAddress = sourceAddress,
                nonce = nonce.value,
                userAgent = userAgent.value,
                startHeight = startHeight.value,
                relay = relay
            )
        }
    }
}