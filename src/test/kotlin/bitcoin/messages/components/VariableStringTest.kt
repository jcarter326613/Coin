package bitcoin.messages.components

import org.junit.jupiter.api.Test

class VariableStringTest {
    @Test
    fun intoAndOutOfArray_Normal_Success() {
        val i = VariableString("Hello world")
        val a = ByteArray(i.calculateMessageSize())

        i.intoByteArray(a, 0)
        val iOut = VariableString.fromByteArray(a, 0)
        assert(i.s == iOut.s)
    }
}