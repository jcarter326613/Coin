package bitcoin.messages

interface IMessage {
    val name: String
    fun toByteArray(): ByteArray
}