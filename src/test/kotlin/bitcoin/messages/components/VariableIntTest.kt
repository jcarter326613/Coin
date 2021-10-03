package bitcoin.messages.components

import org.junit.jupiter.api.Test

class VariableIntTest {
    @Test
    fun intoAndOutOfArray_Normal_Success() {
        val a = ByteArray(5)
        val i = VariableInt(123456789)

        i.intoByteArray(a, 0)
        assert(a[0] == 0xFE.toByte())
        assert(a[1] == 0x15.toByte())
        assert(a[2] == 0xCD.toByte())
        assert(a[3] == 0x5B.toByte())
        assert(a[4] == 0x07.toByte())

        val i2 = VariableInt.fromByteArray(a, 0)
        assert(i2.value.value == i.value)
    }

    @Test
    fun outOfArray_DC_Success() {
        val a = ByteArray(1)
        a[0] = 0xDC.toByte()

        val i = VariableInt.fromByteArray(a, 0)
        assert(i.value.value.toInt() == 220)
    }
}