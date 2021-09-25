package bitcoin

import bitcoin.messages.AddrMessage
import bitcoin.messages.MessageHeader
import bitcoin.messages.VersionMessage

interface IMessageProcessor {
    fun processIncomingMessageVersion(payload: VersionMessage, connection: Connection)
    fun processIncomingMessageAddr(payload: AddrMessage, connection: Connection)
}