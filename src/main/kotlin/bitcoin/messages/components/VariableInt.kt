package bitcoin.messages.components

import util.ByteManipulation
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class VariableInt(
    val value: Long
) {
    fun intoByteArray(dest: ByteArray, destIndex: Int): Int {
        val messageSize = calculateMessageSize()
        when (messageSize) {
            1 -> {
                dest[destIndex] = value.toByte()
            }
            3 -> {
                dest[destIndex] = (0xFD).toByte()
                ByteManipulation.writeShortToArray(value.toShort(), dest, destIndex + 1, ByteOrder.LITTLE_ENDIAN)
            }
            5 -> {
                dest[destIndex] = (0xFE).toByte()
                ByteManipulation.writeIntToArray(value.toInt(), dest, destIndex + 1, ByteOrder.LITTLE_ENDIAN)
            }
            9 -> {
                dest[destIndex] = (0xFF).toByte()
                ByteManipulation.writeLongToArray(value, dest, destIndex + 1, ByteOrder.LITTLE_ENDIAN)
            }
            else -> {
                throw Exception("Invalid size")
            }
        }
        return destIndex + messageSize
    }

    fun calculateMessageSize(): Int {
        return if (value < 0xFD) {
            1
        } else if (value < 0xFFFF) {
            3
        } else if (value < 0xFFFFFFFF) {
            5
        } else {
            9
        }
    }

    companion object {
        fun fromByteArray(buffer: ByteArray, startIndex: Int): VariableInt {
            val leadingByte = buffer[startIndex]
            val value = when (leadingByte) {
                0xFD.toByte() -> {
                    ByteManipulation.readShortFromArray(buffer, startIndex + 1).value
                }
                0xFE.toByte() -> {
                    ByteManipulation.readIntFromArray(buffer, startIndex + 1).value
                }
                0xFF.toByte() -> {
                    ByteManipulation.readLongFromArray(buffer, startIndex + 1).value
                }
                else -> {
                    leadingByte.toLong()
                }
            }.toLong()
            return VariableInt(value)
        }
    }
}