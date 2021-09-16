package bitcoin.messages.components

import java.nio.ByteBuffer

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
                ByteBuffer.allocate(2).putShort(value.toShort()).array().copyInto(dest, destIndex + 1)
            }
            5 -> {
                dest[destIndex] = (0xFE).toByte()
                ByteBuffer.allocate(4).putInt(value.toInt()).array().copyInto(dest, destIndex + 1)
            }
            9 -> {
                dest[destIndex] = (0xFF).toByte()
                ByteBuffer.allocate(8).putLong(value).array().copyInto(dest, destIndex + 1)
            }
            else -> {
                throw Exception("Invalid size")
            }
        }
        return messageSize
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
                    ByteBuffer.allocate(2).put(buffer.slice(startIndex until startIndex + 2).toByteArray()).long
                }
                0xFE -> {
                    ByteBuffer.allocate(4).put(buffer.slice(startIndex until startIndex + 4).toByteArray()).long
                }
                0xFF -> {
                    ByteBuffer.allocate(8).put(buffer.slice(startIndex until startIndex + 8).toByteArray()).long
                }
                else -> {
                    leadingByte.toLong()
                }
            }
            return VariableInt(value)
        }
    }
}