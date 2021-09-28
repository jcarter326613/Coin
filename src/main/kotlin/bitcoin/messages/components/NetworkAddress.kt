package bitcoin.messages.components

import util.ByteManipulation
import java.net.Inet6Address
import java.net.InetAddress
import java.nio.ByteOrder
import java.time.ZonedDateTime

data class NetworkAddress(
    val address: ByteArray,
    val port: Short,
    val serviceFlags: Long
) {
    init {
        if (address.size != 16) {
            throw Exception("Network address incorrect length")
        }
    }

    fun intoByteArray(dest: ByteArray, destIndex: Int, includeTime: Boolean): Int {
        val time = ZonedDateTime.now().toEpochSecond()
        var currentOffset = destIndex
        if (includeTime) {
            currentOffset = ByteManipulation.writeIntToArray(time.toInt(), dest, currentOffset, ByteOrder.LITTLE_ENDIAN)
        }
        currentOffset = ByteManipulation.writeLongToArray(serviceFlags, dest, currentOffset, ByteOrder.LITTLE_ENDIAN)
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

    override fun toString(): String {
        return "${convertByteArrayToAddress(address)} ($port)"
    }

    companion object {
        const val serviceFlagsAll = -1L
        const val serviceFlagsNetwork = 1L                     //https://en.bitcoin.it/wiki/Protocol_documentation

        fun fromByteArray(buffer: ByteArray, startIndex: Int, includeTime: Boolean): NetworkAddress {
            return if (includeTime) {
                NetworkAddress(
                    buffer.slice(startIndex + 12 until startIndex + 28).toByteArray(),
                    ByteManipulation.readShortFromArray(buffer, startIndex + 28, ByteOrder.LITTLE_ENDIAN).value,
                    ByteManipulation.readLongFromArray(buffer, startIndex + 4, ByteOrder.LITTLE_ENDIAN).value
                )
            } else {
                NetworkAddress(
                    buffer.slice(startIndex + 8 until startIndex + 24).toByteArray(),
                    ByteManipulation.readShortFromArray(buffer, startIndex + 24, ByteOrder.LITTLE_ENDIAN).value,
                    ByteManipulation.readLongFromArray(buffer, startIndex, ByteOrder.LITTLE_ENDIAN).value
                )
            }
        }

        fun convertAddressToByteArray(a: String): ByteArray {
            if (a == "0.0.0.0") {
                val address = ByteArray(16)
                for (i in 0..16) {
                    address[0] = 0
                }
                return address
            } else {
                val address = Inet6Address.getByName(a).address
                if (address.size == 4) {
                    val lengthenedAddress = ByteArray(16)
                    for (i in 0..9) {
                        lengthenedAddress[0] = 0
                    }
                    lengthenedAddress[10] = 0xFF.toByte()
                    lengthenedAddress[11] = 0xFF.toByte()
                    lengthenedAddress[12] = address[0]
                    lengthenedAddress[13] = address[1]
                    lengthenedAddress[14] = address[2]
                    lengthenedAddress[15] = address[3]
                    return lengthenedAddress
                }
                return address
            }
        }

        fun convertByteArrayToAddress(a: ByteArray): InetAddress = Inet6Address.getByAddress(a)
    }
}