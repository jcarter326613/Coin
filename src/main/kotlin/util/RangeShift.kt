package util

class RangeShift {
    companion object {
        fun createRange(startIndex: Int, size: Int): Range {
            val nextIndex = startIndex + size
            return Range(
                startIndex until nextIndex,
                nextIndex
            )
        }
    }

    data class Range(
        val range: IntRange,
        val nextI: Int
    )
}