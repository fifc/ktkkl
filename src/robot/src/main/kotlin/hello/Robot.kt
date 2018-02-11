package hello

import com.google.protobuf.Message
import kotlinx.coroutines.experimental.delay
import protofiles.protojava.CodeInProtos
import protofiles.protojava.CommonProtos
import protofiles.protojava.MessagingProto
import java.sql.Timestamp

class Robot constructor(var uid: Long): MqttListener {
    override fun onGameContactInfo(msg: MessagingProto.GameContactInfo, msgUid: Long, mid: Long) {}
    override fun onNearbyUserUpdate(msg: MessagingProto.NearbyUserUpdate, msgUid: Long, mid: Long) {
        //val str = msg.toString()
        //println("$uid -> get ${msg.userListCount} nearby users ...")
    }
    val lngNW: Double = 113.910938
    val latNW: Double = 22.520323
    val lngSE: Double = 113.952137
    val latSE: Double = 22.484561

    var lng: Double = 0.0
    var lat: Double = 0.0

    var http = Http()
    var mqtt = Mqtt(uid, this)

    var status = 0
    var token = ""
    var gameId = 0L

    init {
        Companion.gps(this)
        //println("$uid -> ($lng $lat)")
    }
    private fun getInitParam(){
        var req = CodeInProtos.GetInitParamsRequest.newBuilder()
        req.clientInfoBuilder.uid = uid
        req.clientInfoBuilder.type = CommonProtos.ClientType.IOS
        req.clientInfoBuilder.oem = "Kotlin"
        req.clientInfoBuilder.osVersion = "kt-${uid%1000}"
        req.clientInfoBuilder.version = 1000000300051L
        req.clientInfoBuilder.deviceId = mqtt.clientId_
        req.clientInfoBuilder.gpsBuilder.latitude = lat
        req.clientInfoBuilder.gpsBuilder.longitude = lng
        val reply = CodeInProtos.GetInitParamsReply.newBuilder()
        val start = System.currentTimeMillis()
        Http().gwCall("GetInitParams", req.build(), reply, object: Http.GwCallback {
            override fun onReply(reply: Message) {
                if (reply is CodeInProtos.GetInitParamsReply) {
                    if (reply.hasErrInfo() && reply.errInfo.err != CommonProtos.ErrorCode.OK) {
                        println("$uid GetInitParam error: ${reply.errInfo.msg}")
                        status = -1
                    } else {
                        //val timeCost = System.currentTimeMillis() - start
                        //val str = reply.toString()
                        //println("[$uid] time ${timeCost}ms reply -> $str")
                    }
                }
            }

            override fun onError() {
                val timeCost = System.currentTimeMillis() - start
                println("[$uid] login error. time ${timeCost}ms")
            }

        })
    }

    private suspend fun login() {
        var req = CodeInProtos.LoginRequest.newBuilder()
        req.clientInfoBuilder.uid = uid
        req.passwd = "%03d".format(uid%1000)
        val c = req.clientInfoBuilder
        c.version = 1000000300051L
        c.oem = "Kotlin"
        c.osVersion = "KT-${uid%1000}"
        c.deviceId = mqtt.clientId_
        c.gpsBuilder.latitude = lat
        c.gpsBuilder.longitude = lng
        val reply = CodeInProtos.LoginReply.newBuilder()
        val start = System.currentTimeMillis()
        Http().gwCall("Login", req.build(), reply, object: Http.GwCallback {
            override fun onReply(reply: Message) {
                if (reply is CodeInProtos.LoginReply) {
                    val timeCost = System.currentTimeMillis() - start
                    if (reply.hasErrInfo() && reply.errInfo.err != CommonProtos.ErrorCode.OK) {
                        println("$uid login error: ${reply.errInfo.msg}")
                        status = -1
                    } else {
                       if (reply.token.isEmpty()) {
                           status = -1
                       } else {
                           token = reply.token
                           mqtt.password_ = token
                       }
                    }
                    println("[$uid] login time ${timeCost}ms")
                }
            }

            override fun onError() {
                val timeCost = System.currentTimeMillis() - start
                println("[$uid] login error. time ${timeCost}ms")
            }
        })

        while(token.isEmpty() && status == 0)
            delay(100)

        if (status == 0 && token.isEmpty()) {
            status = -1
        }
    }

    private suspend fun updateNearby() {
        var msg = MessagingProto.SearchNearbyUsers .newBuilder()
        msg.clientInfoBuilder.uid = uid
        msg.clientInfoBuilder.gpsBuilder.latitude = lat
        msg.clientInfoBuilder.gpsBuilder.longitude = lng
        mqtt.sendLiveMsg(msg.build(), MessagingProto.LiveMessageType.LMT_SEARCH_NEARBY_USERS_VALUE)
    }

    private suspend fun init(): Int {
        getInitParam()
        do {
            login()
            if (status != 0) {
                delay(1000)
            }
        } while (status != 0)

        while (!mqtt.connected()) {
            if (!mqtt.start()) {
                delay(1000)
                continue
            }

            for (i in 0..150) {
                if (mqtt.connected())
                    break
                delay(200)
            }
        }


        return 0
    }

    suspend fun runMqtt(): Robot {
        while(!mqtt.connected()) {
            if (!mqtt.start()) {
                delay(1000)
                continue
            }

            for (i in 0..150) {
                if (mqtt.connected())
                    break
                delay(200)
            }
        }

        //val timeStamp = Timestamp(System.currentTimeMillis())
       for (i in 0..3600) {
           var msg = MessagingProto.ImChatMsg.newBuilder()
           msg.toBuilder.uid = 8000000001 + (Math.random() * 5000).toLong()
           msg.fromBuilder.uid = uid
           msg.body = "hello, how are you!"
           mqtt.sendImMsg(msg.build())
           delay(1000)
        }

        stop()
        return this
    }

    suspend fun run(mqttOnly: Boolean = false): Robot {
        status = init()
        if (status != 0) {
            stop()
            return this
        }

        //testBlockUser()

        if (mqttOnly) {
            for (i in 0..3600) {
                var msg = MessagingProto.ImChatMsg.newBuilder()
                msg.toBuilder.uid = 8000000001 + (Math.random() * 5000).toLong()
                msg.fromBuilder.uid = uid
                msg.body = "hello, how are you!"
                mqtt.sendImMsg(msg.build())
                delay(1000)
            }
            stop()
            return this
        }
        //val timeStamp = Timestamp(System.currentTimeMillis())
        var stateTime = 0L
        var nearbyTime = 0L
        val start = System.currentTimeMillis()
        for (i in 1..180) {
            val time = System.currentTimeMillis()
            if (stateTime + 3000 <= time) {
                gps(this)
                stateTime = time
                sendLiveState(MessagingProto.GameLiveState.game_state_ready)
            }

            if (nearbyTime + 5000 <= time) {
                nearbyTime = time
                updateNearby()
            }

            if ((i%10)==0) {
                val elapsed = System.currentTimeMillis() - start
                println("[$uid] i $i elapsed ${elapsed}ms")
            }

            val sleep = time + 1000 - System.currentTimeMillis()
            if (sleep > 0) {
                delay(sleep)
            } else {
                println("[$uid] no time ...")
            }
        }

        sendLiveStateMqtt(MessagingProto.GameLiveState.game_state_idle)
        stop()

        return this
    }

    private fun sendLiveStateMqtt(state: MessagingProto.GameLiveState) {
        var msg = MessagingProto.GameLiveStateInfo.newBuilder()
        msg.state = state
        msg.gameId = gameId
        msg.timeMs = System.currentTimeMillis()
        val c = msg.clientInfoBuilder
        c.uid = uid
        c.oem = "Kotlin"
        c.token = token
        c.deviceId = mqtt.clientId_
        c.osVersion = "kt-${uid%1000}"
        c.type = CommonProtos.ClientType.IOS
        c.gpsBuilder.latitude = lat
        c.gpsBuilder.longitude = lng
        mqtt.sendLiveMsg(msg.build(), MessagingProto.LiveMessageType.LMT_GAME_CONTACT_INFO_VALUE)
    }

    private fun sendLiveState(state: MessagingProto.GameLiveState) {
        var msg = MessagingProto.GameLiveStateInfo.newBuilder()
        msg.state = state
        msg.gameId = gameId
        msg.timeMs = System.currentTimeMillis()
        val c = msg.clientInfoBuilder
        c.uid = uid
        c.oem = "Kotlin"
        c.token = token
        c.deviceId = mqtt.clientId_
        c.osVersion = "kt-${uid%1000}"
        c.type = CommonProtos.ClientType.IOS
        c.gpsBuilder.latitude = lat
        c.gpsBuilder.longitude = lng
        UdpNetty.sendMsg(msg.build(), MessagingProto.LiveMessageType.LMT_GAME_LIVE_STATE_VALUE)
    }

    private fun stop() {
        mqtt.stop()
    }
    private fun stop(code: Int) {
        status = code
        stop()
    }

    companion object {
        private fun gps(robot: Robot) {
            var r = Math.random()
            robot.lng = robot.lngNW + (robot.lngSE - robot.lngNW) * r
            r = Math.random()
            robot.lat = robot.latNW + (robot.latSE - robot.latNW) * r
        }
    }
}