package bitcoin

import bitcoin.messages.MessageHeader
import bitcoin.messages.VersionMessage

interface IMessageProcessor {
    fun processIncomingMessageVersion(header: MessageHeader, payload: VersionMessage, connection: Connection)
}