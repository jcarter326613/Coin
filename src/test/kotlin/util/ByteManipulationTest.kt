package util

import org.junit.jupiter.api.Test
import java.nio.ByteOrder

class ByteManipulationTest {
    @Test
    fun readLongFromArray_AllEndian_Success() {
        val value = 1234567891234L
        val a = ByteArray(8)

        ByteManipulation.writeLongToArray(value, a, 0, ByteOrder.BIG_ENDIAN)
        var o = ByteManipulation.readLongFromArray(a, 0, ByteOrder.BIG_ENDIAN)
        assert(o.value == value)
        assert(o.nextIndex == 8)

        ByteManipulation.writeLongToArray(value, a, 0, ByteOrder.LITTLE_ENDIAN)
        o = ByteManipulation.readLongFromArray(a, 0, ByteOrder.LITTLE_ENDIAN)
        assert(o.value == value)
        assert(o.nextIndex == 8)
    }

    @Test
    fun readIntFromArray_BigEndian_Success() {
        val value = 123456789
        val a = ByteArray(4)

        ByteManipulation.writeIntToArray(value, a, 0, ByteOrder.BIG_ENDIAN)
        var o = ByteManipulation.readIntFromArray(a, 0, ByteOrder.BIG_ENDIAN)
        assert(o.value == value)
        assert(o.nextIndex == 4)

        ByteManipulation.writeIntToArray(value, a, 0, ByteOrder.LITTLE_ENDIAN)
        o = ByteManipulation.readIntFromArray(a, 0, ByteOrder.LITTLE_ENDIAN)
        assert(o.value == value)
        assert(o.nextIndex == 4)
    }

    @Test
    fun readShortFromArray_AllEndian_Success() {
        val value = 1234.toShort()
        val a = ByteArray(2)

        ByteManipulation.writeShortToArray(value, a, 0, ByteOrder.BIG_ENDIAN)
        var o = ByteManipulation.readShortFromArray(a, 0, ByteOrder.BIG_ENDIAN)
        assert(o.value == value)
        assert(o.nextIndex == 2)

        ByteManipulation.writeShortToArray(value, a, 0, ByteOrder.LITTLE_ENDIAN)
        o = ByteManipulation.readShortFromArray(a, 0, ByteOrder.LITTLE_ENDIAN)
        assert(o.value == value)
        assert(o.nextIndex == 2)
    }

    @Test
    fun writeLongToArray_AllEndian_Success() {
        val value = 1234567891234L
        val a = ByteArray(8)

        var i = ByteManipulation.writeLongToArray(value, a, 0, ByteOrder.BIG_ENDIAN)
        assert(i == 8)
        assert(a[0] == 0.toByte())
        assert(a[1] == 0.toByte())
        assert(a[2] == 1.toByte())
        assert(a[3] == 31.toByte())
        assert(a[4] == 113.toByte())
        assert(a[5] == 0xFB.toByte())
        assert(a[6] == 9.toByte())
        assert(a[7] == 34.toByte())

        i = ByteManipulation.writeLongToArray(value, a, 0, ByteOrder.LITTLE_ENDIAN)
        assert(i == 8)
        assert(a[7] == 0.toByte())
        assert(a[6] == 0.toByte())
        assert(a[5] == 1.toByte())
        assert(a[4] == 31.toByte())
        assert(a[3] == 113.toByte())
        assert(a[2] == 0xFB.toByte())
        assert(a[1] == 9.toByte())
        assert(a[0] == 34.toByte())
    }

    @Test
    fun writeIntToArray_AllEndian_Success() {
        val value = 123456789
        val a = ByteArray(4)

        var i = ByteManipulation.writeIntToArray(value, a, 0, ByteOrder.BIG_ENDIAN)
        assert(i == 4)
        assert(a[0] == 7.toByte())
        assert(a[1] == 91.toByte())
        assert(a[2] == 0xCD.toByte())
        assert(a[3] == 21.toByte())

        i = ByteManipulation.writeIntToArray(value, a, 0, ByteOrder.LITTLE_ENDIAN)
        assert(i == 4)
        assert(a[3] == 7.toByte())
        assert(a[2] == 91.toByte())
        assert(a[1] == 0xCD.toByte())
        assert(a[0] == 21.toByte())
    }

    @Test
    fun writeShortToArray_AllEndian_Success() {
        val value = 1234.toShort()
        val a = ByteArray(2)

        var i = ByteManipulation.writeShortToArray(value, a, 0, ByteOrder.BIG_ENDIAN)
        assert(i == 2)
        assert(a[0] == 4.toByte())
        assert(a[1] == 0xD2.toByte())

        i = ByteManipulation.writeShortToArray(value, a, 0, ByteOrder.LITTLE_ENDIAN)
        assert(i == 2)
        assert(a[1] == 4.toByte())
        assert(a[0] == 0xD2.toByte())
    }
}