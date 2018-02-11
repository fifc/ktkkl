package hello

import java.io.*
import java.net.*
import com.google.protobuf.Message
import protofiles.protojava.CommonProtos as CommonProto
import protofiles.protojava.MessagingProto

object Udp {
    private val port_ = 1000
    private val address_ = InetAddress.getByName("u.codein.net")
    private val socket_ = DatagramSocket()

    init {
    }

    fun destroy() {
       socket_.close()
    }

    fun sendMsg(msg: Message, type: Int, id: Long = 0) {
       var body = msg.toByteArray()
       val payload = ByteArray(size = body.size + 10)
       payload[0] = ((type shr 8) and 0xff).toByte()
       payload[1] = (type and 0xff).toByte()
       payload[2] = (id shr 56).toByte()
       payload[3] = (id shr 48).toByte()
       payload[4] = (id shr 40).toByte()
       payload[5] = (id shr 32).toByte()
       payload[6] = (id shr 24).toByte()
       payload[7] = (id shr 16).toByte()
       payload[8] = (id shr 8).toByte()
       payload[9] = id.toByte()

       (0 until body.size).forEach {
           payload[it + 10] = body[it]
       }

       var packet = DatagramPacket(payload, payload.size, address_, port_)
       println("Udp send size: ${payload.size}")
       socket_.send(packet)

       // get response
       //packet = DatagramPacket(payload, payload.size)
       //socket_.receive(packet)

       // display response
       //val received = String(packet.data, 0, packet.length)
       //println("Udp response size: ${packet.length}")
    }

    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        var b = MessagingProto.GameLiveStateInfo.newBuilder()
        b.gameId = 1234L
        b.state = MessagingProto.GameLiveState.game_state_ready
        val c = b.clientInfoBuilder
        c.uid = 8000000001L
        c.version = 1000000300000L
        c.type = CommonProto.ClientType.IOS
        c.oem = "Kotlin"
        c.gpsBuilder.longitude = 113.12345678
        c.gpsBuilder.latitude = 22.87654321
        c.deviceId = "kkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk"
        c.token = "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"

        val msg = b.build()
        val str = msg.toString()
        println("msg -> $str")

        sendMsg(msg, MessagingProto.LiveMessageType.LMT_GAME_LIVE_STATE_VALUE, 123L)
    }
}
