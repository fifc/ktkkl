package hello

import io.netty.bootstrap.Bootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.DatagramPacket
import io.netty.channel.socket.nio.NioDatagramChannel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import java.net.InetSocketAddress

import com.google.protobuf.Message
import io.netty.channel.Channel
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import protofiles.protojava.MessagingProto
import java.net.Inet6Address
import protofiles.protojava.CommonProtos as CommonProto

import kotlin.experimental.and

object UdpNetty {
    private val channel_ : Channel
    private val group_ = NioEventLoopGroup()
    private val addr_ = InetSocketAddress("u.codein.net", 1000)
    //private val addr_ = InetSocketAddress("[2001:2:0:1baa::276c:efaa]", 1000)
    init {
        val b = Bootstrap()
        b.group(group_).channel(NioDatagramChannel::class.java)
                //.option(ChannelOption.SO_BROADCAST,true)
                //.handler(UdpClientHandler())
                .handler(object: SimpleChannelInboundHandler<DatagramPacket>(){
                    @Throws(Exception::class)
                    public override fun messageReceived(channelHandlerContext: ChannelHandlerContext,
                                                        packet: DatagramPacket) {
                        var bbf = packet.content()
                        var arr = ByteArray(bbf.writerIndex())
                        bbf.readBytes(arr)
                        //println("udp response size ${ba.size}")
                        //channelHandlerContext.close()            val arr = msg.payload as ByteArray
                        val type = (arr[0] and 0xff.toByte()).toInt() shl 8 or
                                (arr[1] and 0xff.toByte()).toInt()
                        val msgId = (arr[0] and 0xff.toByte()).toLong() shl 56 or
                                ((arr[3] and 0xff.toByte()).toLong() shl 48) or
                                ((arr[4] and 0xff.toByte()).toLong() shl 40) or
                                ((arr[5] and 0xff.toByte()).toLong() shl 32) or
                                ((arr[6] and 0xff.toByte()).toLong() shl 24) or
                                ((arr[7] and 0xff.toByte()).toLong() shl 16) or
                                ((arr[8] and 0xff.toByte()).toLong() shl 8) or
                                (arr[9] and 0xff.toByte()).toLong()

                        try {
                            when (MessagingProto.LiveMessageType.forNumber(type)) {
                                MessagingProto.LiveMessageType.LMT_SERVICE_RESPONSE -> {
                                    var udpMsg = MessagingProto.ServiceResponse.parseFrom(arr.sliceArray(10 until arr.size))
                                    //println("[UDP] response -> $udpMsg")
                                }
                                else -> {
                                    println("[UDP] response -> unknown msg type $type")
                                }
                            }
                        } catch (e: com.google.protobuf.InvalidProtocolBufferException) {
                            e.printStackTrace()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    @Throws(Exception::class)
                    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
                        ctx.close()
                        cause.printStackTrace()
                    }
                })

        channel_ = b.bind(0).sync().channel()
    }

    fun close() {
        group_.shutdownGracefully()
        if (!channel_.closeFuture().await(100)) {
            println("close timeout!")
        }
    }
    @Throws(Exception::class)
    fun sendMsg(msg: Message, type: Int, id: Long = 0) {
        var body = msg.toByteArray()
        val payload = ByteArray(size = body.size + 10)
        payload[0] = ((type shr 7) and 0xff).toByte()
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

        var packet = DatagramPacket(Unpooled.copiedBuffer(payload), addr_)
        try {
            channel_.writeAndFlush( packet).sync()

        } finally {
        }
    }


    @Throws(Exception::class)
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

        runBlocking {
            delay(1000)
        }
    }
}
