package bitcoin.messages

import bitcoin.Connection
import java.nio.ByteBuffer

data class MessageHeader(
    val magic: Int,
    val command: String,
    val payloadLength: Int,
    val checksum: Int
) {
    fun toByteArray(): ByteArray {
        val commandArray = command.toByteArray()
        val array = ByteArray(Connection.messageHeaderSize)

        ByteBuffer.allocate(4).putInt(magic).array().copyInto(array)
        commandArray.copyInto(array, 4)
        for (i in commandArray.size..12) {
            array[4+i] = 0
        }

        ByteBuffer.allocate(4).putInt(payloadLength).array().copyInto(array, 16)
        ByteBuffer.allocate(4).putInt(checksum).array().copyInto(array, 20)
        return array
    }

    companion object {
        fun fromByteArray(buffer: ByteArray): MessageHeader {
            return MessageHeader(   //https://en.bitcoin.it/wiki/Protocol_documentation#Message_structure
                ByteBuffer.allocate(4).put(buffer.slice(0 until 4).toByteArray()).int,
                ByteBuffer.allocate(12).put(buffer.slice(4 until 16).toByteArray()).toString(),
                ByteBuffer.allocate(4).put(buffer.slice(16 until 20).toByteArray()).int,
                ByteBuffer.allocate(4).put(buffer.slice(20 until 24).toByteArray()).int,
            )
        }
    }
}