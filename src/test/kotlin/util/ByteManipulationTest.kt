package util

import org.junit.jupiter.api.Test
import java.nio.ByteOrder

class ByteManipulationTest {
    @Test
    fun readLongFromArray_BigEndian_Success() {
        val value = 1234567891234L
        val a = ByteArray(8)
        ByteManipulation.writeLongToArray(value, a, 0, ByteOrder.BIG_ENDIAN)
        val o = ByteManipulation.readLongFromArray(a, 0, ByteOrder.BIG_ENDIAN)
        assert(o.value == value)
        assert(o.nextIndex == 8)
    }

    @Test
    fun readLongFromArray_LittleEndian_Success() {
        val value = 1234567891234L
        val a = ByteArray(8)
        ByteManipulation.writeLongToArray(value, a, 0, ByteOrder.LITTLE_ENDIAN)
        val o = ByteManipulation.readLongFromArray(a, 0, ByteOrder.LITTLE_ENDIAN)
        assert(o.value == value)
        assert(o.nextIndex == 8)
    }

    @Test
    fun writeLongToArray_BigEndian_Success() {
        val value = 1234567891234L
        val a = ByteArray(8)
        ByteManipulation.writeLongToArray(value, a, 0, ByteOrder.BIG_ENDIAN)
        assert(a[0] == 0.toByte())
        assert(a[1] == 0.toByte())
        assert(a[2] == 1.toByte())
        assert(a[3] == 31.toByte())
        assert(a[4] == 113.toByte())
        assert(a[5] == 0xFB.toByte())
        assert(a[6] == 9.toByte())
        assert(a[7] == 34.toByte())
    }

    @Test
    fun writeIntToArray_BigEndian_Success() {
        val value = 123456789
        val a = ByteArray(4)
        ByteManipulation.writeIntToArray(value, a, 0, ByteOrder.BIG_ENDIAN)
        assert(a[0] == 7.toByte())
        assert(a[1] == 91.toByte())
        assert(a[2] == 0xCD.toByte())
        assert(a[3] == 21.toByte())
    }
}