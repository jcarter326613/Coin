package bitcoin.messages.components

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
                ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(value.toShort()).array().copyInto(dest, destIndex + 1)
            }
            5 -> {
                dest[destIndex] = (0xFE).toByte()
                ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value.toInt()).array().copyInto(dest, destIndex + 1)
            }
            9 -> {
                dest[destIndex] = (0xFF).toByte()
                ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(value).array().copyInto(dest, destIndex + 1)
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
            val value = when (leadingByte.toInt()) {
                0xFD -> {
                    ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).put(buffer.slice(startIndex until startIndex + 2).toByteArray()).getLong(0)
                }
                0xFE -> {
                    ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).put(buffer.slice(startIndex until startIndex + 4).toByteArray()).getLong(0)
                }
                0xFF -> {
                    ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).put(buffer.slice(startIndex until startIndex + 8).toByteArray()).getLong(0)
                }
                else -> {
                    leadingByte.toLong()
                }
            }
            return VariableInt(value)
        }
    }
}