package bitcoin.messages.components

import org.junit.jupiter.api.Test
import util.ByteManipulation

class NetworkAddressTest {
    @Test
    fun intoAndOutOfArray_Normal_Success() {
        val ipArray = ByteArray(16)
        ipArray[0] = 127
        ipArray[3] = 10

        val n = NetworkAddress(ipArray, 8080)
        val a = ByteArray(n.calculateMessageSize(true))

        n.intoByteArray(a, 0, true, false)
        val nOut = NetworkAddress.fromByteArray(a, 0, true)
        assert(nOut.address.contentEquals(n.address))
        assert(nOut.port == n.port)
    }
}