package util

class Log {
    companion object {
        fun error(e: Throwable) {
            println(e.message)
            println(e.stackTraceToString())
        }

        fun error(e: String) {
            error(Exception(e))
        }

        fun info(m: String) {
            println("INFO: $m")
        }

        fun info(b: ByteArray) {
            for (i in b) {
                print(Integer.toHexString(i.toUByte().toInt()))
                print(" ")
            }
            println()
        }
    }
}