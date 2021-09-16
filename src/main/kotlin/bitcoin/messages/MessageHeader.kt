package bitcoin.messages

import bitcoin.Connection
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class MessageHeader(
    val magic: Int,
    val command: String,
    val payloadLength: Int,
    val checksum: Int
) {
    fun toByteArray(): ByteArray {
        val commandArray = command.toByteArray()
        val array = ByteArray(Connection.messageHeaderSize)

        ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(magic).array().copyInto(array)
        commandArray.copyInto(array, 4)
        for (i in commandArray.size..12) {
            array[4+i] = 0
        }

        ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(payloadLength).array().copyInto(array, 16)
        ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(checksum).array().copyInto(array, 20)
        return array
    }

    companion object {
        fun fromByteArray(buffer: ByteArray): MessageHeader {
            return MessageHeader(   //https://en.bitcoin.it/wiki/Protocol_documentation#Message_structure
                ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).put(buffer.slice(0 until 4).toByteArray()).int,
                ByteBuffer.allocate(12).order(ByteOrder.LITTLE_ENDIAN).put(buffer.slice(4 until 16).toByteArray()).toString(),
                ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).put(buffer.slice(16 until 20).toByteArray()).int,
                ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).put(buffer.slice(20 until 24).toByteArray()).int,
            )
        }
    }
}