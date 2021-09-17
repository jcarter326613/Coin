package bitcoin.messages

import bitcoin.Connection
import util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MessageHeader(
    val magic: Int,
    val command: String,
    val payloadLength: Int,
    val checksum: ByteArray
) {
    init {
        if (checksum.size != 4) {
            Log.info(checksum)
            throw Exception("Checksum incorrect size")
        }
    }

    fun toByteArray(): ByteArray {
        val commandArray = command.toByteArray()
        val array = ByteArray(Connection.messageHeaderSize)

        ByteBuffer.allocate(4).putInt(magic).array().copyInto(array)
        commandArray.copyInto(array, 4)
        for (i in commandArray.size..12) {
            array[4+i] = 0
        }

        ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(payloadLength).array().copyInto(array, 16)
        checksum.copyInto(array, 20)
        return array
    }

    companion object {
        fun fromByteArray(buffer: ByteArray): MessageHeader {
            var command = String(buffer.slice(4 until 16).toByteArray())
            val zeroIndex = command.indexOf((0).toChar())
            if (zeroIndex >= 0) {
                command = command.substring(0, zeroIndex)
            }

            return MessageHeader(   //https://en.bitcoin.it/wiki/Protocol_documentation#Message_structure
                ByteBuffer.allocate(4).put(buffer.slice(0 until 4).toByteArray()).getInt(0),
                command,
                ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).put(buffer.slice(16 until 20).toByteArray()).getInt(0),
                buffer.slice(20 until 24).toByteArray()
            )
        }
    }
}