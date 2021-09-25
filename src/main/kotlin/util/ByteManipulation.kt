package util

import java.nio.ByteOrder

class ByteManipulation {
    companion object {
        fun writeLongToArray(vIn: Long, target: ByteArray, offset: Int, endian: ByteOrder = ByteOrder.LITTLE_ENDIAN): Int {
            var v = vIn
            val range = when (endian) {
                ByteOrder.LITTLE_ENDIAN -> 0 until 8
                ByteOrder.BIG_ENDIAN -> 7 downTo 0
                else -> throw Exception("Invalid endian")
            }

            for (i in range) {
                target[offset + i] = (v and 0xFFL).toByte()
                v = v ushr 8
            }

            return offset + 8
        }

        fun writeIntToArray(vIn: Int, target: ByteArray, offset: Int, endian: ByteOrder = ByteOrder.LITTLE_ENDIAN): Int {
            var v = vIn
            val range = when (endian) {
                ByteOrder.LITTLE_ENDIAN -> 0 until 4
                ByteOrder.BIG_ENDIAN -> 3 downTo 0
                else -> throw Exception("Invalid endian")
            }

            for (i in range) {
                target[offset + i] = (v and 0xFF).toByte()
                v = v ushr 8
            }

            return offset + 4
        }

        fun writeShortToArray(vIn: Short, target: ByteArray, offset: Int, endian: ByteOrder = ByteOrder.LITTLE_ENDIAN): Int {
            var v = vIn.toInt()
            val range = when (endian) {
                ByteOrder.LITTLE_ENDIAN -> 0 until 2
                ByteOrder.BIG_ENDIAN -> 1 downTo 0
                else -> throw Exception("Invalid endian")
            }

            for (i in range) {
                target[offset + i] = (v and 0xFF).toByte()
                v = v ushr 8
            }

            return offset + 2
        }

        fun readLongFromArray(source: ByteArray, offset: Int, endian: ByteOrder = ByteOrder.LITTLE_ENDIAN): LongResult {
            var retVal: Long = 0

            val range = when (endian) {
                ByteOrder.LITTLE_ENDIAN -> 0 until 8
                ByteOrder.BIG_ENDIAN -> 7 downTo 0
                else -> throw Exception("Invalid endian")
            }

            for ((bI, i) in range.withIndex()) {
                retVal = retVal or (source[offset + i].toUByte().toLong() shl (bI * 8))
            }

            return LongResult(retVal, offset + 8)
        }

        fun readIntFromArray(source: ByteArray, offset: Int, endian: ByteOrder = ByteOrder.LITTLE_ENDIAN): IntResult {
            var retVal: Int = 0

            val range = when (endian) {
                ByteOrder.LITTLE_ENDIAN -> 0 until 4
                ByteOrder.BIG_ENDIAN -> 3 downTo 0
                else -> throw Exception("Invalid endian")
            }

            for ((bI, i) in range.withIndex()) {
                retVal = retVal or (source[offset + i].toUByte().toInt() shl (bI * 8))
            }

            return IntResult(retVal, offset + 4)
        }

        fun readShortFromArray(source: ByteArray, offset: Int, endian: ByteOrder = ByteOrder.LITTLE_ENDIAN): ShortResult {
            var retVal: Int = 0

            val range = when (endian) {
                ByteOrder.LITTLE_ENDIAN -> 0 until 2
                ByteOrder.BIG_ENDIAN -> 1 downTo 0
                else -> throw Exception("Invalid endian")
            }

            for ((bI, i) in range.withIndex()) {
                retVal = retVal or (source[offset + i].toUByte().toInt() shl (bI * 8))
            }

            return ShortResult(retVal.toShort(), offset + 2)
        }
    }

    data class LongResult(val value: Long, val nextIndex: Int)
    data class IntResult(val value: Int, val nextIndex: Int)
    data class ShortResult(val value: Short, val nextIndex: Int)
}