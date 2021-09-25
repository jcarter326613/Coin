package bitcoin.messages

import org.junit.jupiter.api.Test

class MessageHeaderTest {
    @Test
    fun inAndOut_Normal_Success() {
        val checksum = ByteArray(4)
        checksum[0] = 4
        checksum[1] = 76
        checksum[2] = 100
        checksum[3] = -12
        val header = MessageHeader(magic = 123, command = "TestCommand", payloadLength = 654, checksum = checksum)
        val headerByteArray = header.toByteArray()
        val outHeader = MessageHeader.fromByteArray(headerByteArray)

        assert(header.checksum.contentEquals(outHeader.checksum))
        assert(header.command == outHeader.command)
        assert(header.magic == outHeader.magic)
        assert(header.payloadLength == outHeader.payloadLength)
    }
}