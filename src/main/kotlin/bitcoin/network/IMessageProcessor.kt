package bitcoin.network

import bitcoin.messages.AddrMessage
import bitcoin.messages.InvMessage
import bitcoin.messages.RejectMessage
import bitcoin.messages.VersionMessage

interface IMessageProcessor {
    fun processIncomingMessageVersion(payload: VersionMessage, connection: Connection)
    fun processIncomingMessageAddr(payload: AddrMessage, connection: Connection)
    fun processIncomingMessageInv(payload: InvMessage, connection: Connection)
    fun processIncomingMessageReject(payload: RejectMessage, connection: Connection)
}
