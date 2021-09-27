package bitcoin.network

import bitcoin.messages.AddrMessage
import bitcoin.messages.InvMessage
import bitcoin.messages.VersionMessage

interface IMessageProcessor {
    fun processIncomingMessageVersion(payload: VersionMessage, connection: Connection)
    fun processIncomingMessageAddr(payload: AddrMessage, connection: Connection)
    fun processIncomingMessageInv(payload: InvMessage, connection: Connection)
}