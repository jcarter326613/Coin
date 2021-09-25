package bitcoin.messages.components

import bitcoin.Connection
import util.ByteManipulation
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.time.ZonedDateTime

data class NetworkAddress(
    val address: ByteArray,
    val port: Short
) {
    init {
        if (address.size != 16) {
            throw Exception("Network address incorrect length")
        }
    }

    fun intoByteArray(dest: ByteArray, destIndex: Int, includeTime: Boolean, includeAllServices: Boolean): Int {
        val time = ZonedDateTime.now().toEpochSecond()
        var currentOffset = destIndex
        if (includeTime) {
            currentOffset = ByteManipulation.writeIntToArray(time.toInt(), dest, currentOffset, ByteOrder.LITTLE_ENDIAN)
        }
        if (includeAllServices) {
            currentOffset = ByteManipulation.writeLongToArray(-1L, dest, currentOffset, ByteOrder.LITTLE_ENDIAN)
        } else {
            currentOffset = ByteManipulation.writeLongToArray(Connection.servicesBitFlag, dest, currentOffset, ByteOrder.LITTLE_ENDIAN)
        }
        address.copyInto(dest, currentOffset)
        currentOffset += address.size
        currentOffset = ByteManipulation.writeShortToArray(port, dest, currentOffset, ByteOrder.LITTLE_ENDIAN)
        return currentOffset
    }

    fun calculateMessageSize(includeTime: Boolean): Int {
        return if (includeTime) {
            4 + 8 + 16 + 2
        } else {
            8 + 16 + 2
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NetworkAddress

        if (!address.contentEquals(other.address)) return false
        if (port != other.port) return false

        return true
    }

    override fun hashCode(): Int {
        var result = address.contentHashCode()
        result = 31 * result + port
        return result
    }

    companion object {
        fun fromByteArray(buffer: ByteArray, startIndex: Int, includeTime: Boolean): NetworkAddress {
            return if (includeTime) {
                NetworkAddress(
                    buffer.slice(startIndex + 12 until startIndex + 28).toByteArray(),
                    ByteManipulation.readShortFromArray(buffer, startIndex + 28, ByteOrder.LITTLE_ENDIAN).value
                )
            } else {
                NetworkAddress(
                    buffer.slice(startIndex + 8 until startIndex + 24).toByteArray(),
                    ByteManipulation.readShortFromArray(buffer, startIndex + 24, ByteOrder.LITTLE_ENDIAN).value
                )
            }
        }
    }
}