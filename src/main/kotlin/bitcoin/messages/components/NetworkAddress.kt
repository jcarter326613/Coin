package bitcoin.messages.components

import bitcoin.Connection
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
            ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(time.toInt()).array().copyInto(dest, currentOffset)
            currentOffset += 4
        }
        if (includeAllServices) {
            ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(-1L).array().copyInto(dest, currentOffset)
        } else {
            ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(Connection.servicesBitFlag).array().copyInto(dest, currentOffset)
        }
        currentOffset += 8
        address.copyInto(dest, currentOffset)
        currentOffset += 16
        ByteBuffer.allocate(2).putShort(port).array().copyInto(dest, currentOffset)
        return currentOffset + 2
    }

    fun calculateMessageSize(includeTime: Boolean): Int {
        return if (includeTime) {
            4 + 8 + 16 + 2
        } else {
            8 + 16 + 2
        }
    }

    companion object {
        fun fromByteArray(buffer: ByteArray, startIndex: Int, includeTime: Boolean): NetworkAddress {
            return if (includeTime) {
                NetworkAddress(
                    buffer.slice(startIndex + 12 until startIndex + 28).toByteArray(),
                    ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).put(buffer.slice(startIndex + 28 until startIndex + 30).toByteArray()).getShort(0)
                )
            } else {
                NetworkAddress(
                    buffer.slice(startIndex + 8 until startIndex + 24).toByteArray(),
                    ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).put(buffer.slice(startIndex + 24 until startIndex + 26).toByteArray()).getShort(0)
                )
            }
        }
    }
}