package util

import java.nio.ByteOrder

class ByteManipulation {
    companion object {
        fun writeIntToArray(v: Int, target: ByteArray, offset: Int, endian: ByteOrder = ByteOrder.LITTLE_ENDIAN): Int {
            var v = v
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

        fun writeLongToArray(v: Long, target: ByteArray, offset: Int, endian: ByteOrder = ByteOrder.LITTLE_ENDIAN): Int {
            var v = v
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

        fun readLongFromArray(source: ByteArray, offset: Int, endian: ByteOrder = ByteOrder.LITTLE_ENDIAN): LongResult {
            var retVal: Long = 0

            val range = when (endian) {
                ByteOrder.LITTLE_ENDIAN -> 0 until 8
                ByteOrder.BIG_ENDIAN -> 7 downTo 0
                else -> throw Exception("Invalid endian")
            }

            var bI = 0
            for (i in range) {
                retVal = retVal or (source[offset + i].toUByte().toLong() shl (bI * 8))
                bI++
            }

            return LongResult(retVal, offset + 8)
        }
    }

    data class LongResult(val value: Long, val nextIndex: Int)
}